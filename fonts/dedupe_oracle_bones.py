#!/usr/bin/env python3
import sys, os, csv, math, hashlib, io
from collections import defaultdict
from dataclasses import dataclass
import numpy as np
from PIL import Image, ImageOps
import imagehash
from fontTools.ttLib import TTFont
import freetype
from skimage.measure import moments_hu

new_start = 0x2F000
max_glyphs = 0x2FFFF - 0x2F000

# ---------- rendering ----------
def render_glyph_bitmap(face, glyph_index, px, pad):
    face.set_pixel_sizes(0, px - 2*pad)
    # Load by glyph index (works for unencoded glyphs)
    face.load_glyph(glyph_index, freetype.FT_LOAD_NO_HINTING | freetype.FT_LOAD_NO_AUTOHINT | freetype.FT_LOAD_RENDER)
    slot = face.glyph
    bmp = slot.bitmap
    if bmp.rows == 0 or bmp.width == 0:
        return Image.new("L", (px, px), 0)
    arr = np.array(bmp.buffer, dtype=np.uint8).reshape(bmp.rows, bmp.width)
    img = Image.fromarray(arr, mode="L")

    # Put into square canvas
    canvas = Image.new("L", (bmp.width, bmp.rows), 0)
    canvas.paste(img, (0,0))

    # crop tight, then pad + center to 128x128, preserving aspect
    bbox = canvas.getbbox()
    if bbox is None:
        return Image.new("L", (px, px), 0)
    cropped = canvas.crop(bbox)
    # normalize size to fit into px-2*pad
    w, h = cropped.size
    scale = min((px-2*pad)/w, (px-2*pad)/h) if max(w,h) else 1.0
    nw, nh = max(1, int(round(w*scale))), max(1, int(round(h*scale)))
    resized = cropped.resize((nw, nh), Image.BILINEAR)
    out = Image.new("L", (px, px), 0)
    out.paste(resized, ((px-nw)//2, (px-nh)//2))
    # normalize brightness
    out = ImageOps.autocontrast(out)
    return out

# ---------- descriptors ----------
def descriptor(img: Image.Image):
    # pHash (64-bit) + Hu moments (8 floats)
    ph = imagehash.phash(img)  # 8x8 DCT
    # binarize lightly for moments
    arr = np.array(img, dtype=np.float32)/255.0
    thr = np.clip(arr.mean()*0.6, 0.1, 0.8)
    binm = (arr > thr).astype(np.float32)
    hu = moments_hu(binm)  # 7 moments; we add area as 8th
    area = float(binm.sum())/binm.size
    return int(str(ph), 16), np.concatenate([hu.astype(np.float64), [area]])

def hamming64(a:int, b:int) -> int:
    return (a ^ b).bit_count()

def hu_dist(a:np.ndarray, b:np.ndarray) -> float:
    # log transform for stability on Hu moments; Euclidean in log-space
    la = np.sign(a)*np.log1p(np.abs(a))
    lb = np.sign(b)*np.log1p(np.abs(b))
    return float(np.linalg.norm(la - lb))

# ---------- simple LSH bucketing on pHash ----------
def phash_bands(h: int, bands=4, bits_per_band=16):
    # 4 bands × 16 bits = 64 bits
    keys=[]
    for i in range(bands):
        shift = (bands-1-i)*bits_per_band
        mask = (1<<bits_per_band)-1
        keys.append((i, (h>>shift)&mask))
    return keys

# ---------- union-find ----------
class UF:
    def __init__(self,n): self.p=list(range(n)); self.r=[0]*n
    def f(self,x):
        while self.p[x]!=x:
            self.p[x]=self.p[self.p[x]]; x=self.p[x]
        return x
    def u(self,a,b):
        ra,rb=self.f(a),self.f(b)
        if ra==rb: return
        if self.r[ra]<self.r[rb]: ra,rb=rb,ra
        self.p[rb]=ra
        if self.r[ra]==self.r[rb]: self.r[ra]+=1

@dataclass
class GlyphRec:
    index:int
    name:str
    codepoints:list  # may be empty
    ph:int
    hu:np.ndarray
    img:Image.Image

def glyph_name_to_cps(tt):
    # map glyphName -> [codepoints]
    cps_by_name = defaultdict(list)
    for t in tt["cmap"].tables:
        if not hasattr(t, "cmap"): continue
        for cp, name in t.cmap.items():
            cps_by_name[name].append(cp)
    return cps_by_name

def main(font_path:str, out_dir:str, max_glyphs=None, ham_thr=8, hu_thr=1.6):
    os.makedirs(out_dir, exist_ok=True)

    # open with fontTools for names, with freetype for rendering
    tt = TTFont(font_path, lazy=True)
    face = freetype.Face(font_path)

    glyph_order = tt.getGlyphOrder()
    cps_by_name = glyph_name_to_cps(tt)
    nG = len(glyph_order)
    if max_glyphs: nG = min(nG, max_glyphs)

    recs=[]
    for gi in range(nG):
        name = glyph_order[gi]
        # skip .notdef etc.
        if name.startswith(".notdef"): continue
        try:
            img = render_glyph_bitmap(face, gi, px=128, pad=2)
        except Exception:
            continue
        ph, hu = descriptor(img)
        recs.append(GlyphRec(index=gi, name=name, codepoints=cps_by_name.get(name, []), ph=ph, hu=hu, img=img))

    # Sequential comparison: each glyph with the next one only
    # Group consecutive duplicates
    groups = []  # list of (group_id, list of indices in group)
    in_group = [False] * len(recs)
    group_id = 0

    for i in range(len(recs) - 1):
        if in_group[i]:
            continue
        # Start a potential group with current glyph
        current_group = [i]
        in_group[i] = True

        # Check if next glyph matches current
        j = i + 1
        while j < len(recs):
            if in_group[j]:
                break
            a = recs[j - 1]
            b = recs[j]
            if hamming64(a.ph, b.ph) <= ham_thr and hu_dist(a.hu, b.hu) <= hu_thr:
                current_group.append(j)
                in_group[j] = True
                j += 1
            else:
                break

        groups.append((group_id, current_group))
        group_id += 1

    # Add any remaining ungrouped glyphs as single-element groups
    for i in range(len(recs)):
        if not in_group[i]:
            groups.append((group_id, [i]))
            group_id += 1

    # Convert to clusters dict for compatibility
    clusters = {gid: idxs for gid, idxs in groups}

    # pick medoid per cluster (min average distance in log-Hu space + hamming/64)
    reps = {}
    for root, idxs in clusters.items():
        if len(idxs)==1:
            reps[root]=idxs[0]
            continue
        mat_hu = np.zeros((len(idxs), len(idxs)), dtype=np.float64)
        mat_hm = np.zeros_like(mat_hu)
        for a in range(len(idxs)):
            ra = recs[idxs[a]]
            for b in range(a+1, len(idxs)):
                rb = recs[idxs[b]]
                dh = hu_dist(ra.hu, rb.hu)
                hm = hamming64(ra.ph, rb.ph)/64.0
                mat_hu[a,b]=mat_hu[b,a]=dh
                mat_hm[a,b]=mat_hm[b,a]=hm
        D = 0.7*mat_hu + 0.3*mat_hm
        s = D.sum(axis=1)
        reps[root] = idxs[int(np.argmin(s))]

    # write CSV mapping
    csv_path = os.path.join(out_dir, "clusters.csv")
    with open(csv_path, "w", newline="") as f:
        w = csv.writer(f)
        w.writerow(["group_id","rep_glyph_index","rep_glyph_name","member_glyph_index","member_glyph_name","member_codepoints_hex","group_size"])
        for gid, idxs in sorted(clusters.items()):
            rep = reps[gid]
            rep_rec = recs[rep]
            for i in idxs:
                r = recs[i]
                w.writerow([
                    gid,
                    rep_rec.index, rep_rec.name,
                    r.index, r.name,
                    " ".join(f"U+{cp:04X}" for cp in r.codepoints),
                    len(idxs)
                ])

    # export representative sheet
    reps_imgs = [recs[reps[root]].img for root in sorted(clusters.keys(), key=lambda k: len(clusters[k]), reverse=True)]
    if reps_imgs:
        cols = 32
        rows = math.ceil(len(reps_imgs)/cols)
        cell = 128
        sheet = Image.new("L", (cols*cell, rows*cell), 255)
        for k, im in enumerate(reps_imgs):
            r, c = divmod(k, cols)
            sheet.paste(im, (c*cell, r*cell))
        sheet.convert("RGB").save(os.path.join(out_dir, "representatives.png"))

    # small stats
    sizes = sorted((len(v) for v in clusters.values()), reverse=True)
    with open(os.path.join(out_dir,"stats.txt"), "w") as f:
        print(f"glyphs: {len(recs)}", file=f)
        print(f"clusters: {len(clusters)}", file=f)
        print(f"avg cluster size: {np.mean(sizes):.2f}", file=f)
        print(f"top 20 cluster sizes: {sizes[:20]}", file=f)

    print(csv_path)
    print(os.path.join(out_dir, "representatives.png"))
    print(os.path.join(out_dir, "stats.txt"))

if __name__ == "__main__":
    if len(sys.argv)<3:
        print("usage: python obs_dedupe.py FONT.ttf out_dir [max_glyphs]")
        # sys.exit(2)
    font_path = sys.argv[1] if len(sys.argv)>1 else "oracle_bone_script_F0000.ttf"
    out_dir = sys.argv[2]  if len(sys.argv)>2 else "oracles"
    max_glyphs = int(sys.argv[3]) if len(sys.argv)>3 else max_glyphs
    # thresholds: ham_thr (0..64), hu_thr (~1.2..2.0 tighter→fewer merges)
    main(font_path, out_dir, max_glyphs=max_glyphs, ham_thr=8, hu_thr=1.6)