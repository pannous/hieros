"""Render sample rows of docs/oracle_seal.md (hanzi | oracle | seal) to verify the alignment visually."""
from PIL import Image, ImageDraw, ImageFont
SAMPLE = ["不", "人", "王", "中", "大", "子", "女", "天", "帝", "示", "馬", "鳥"]
FONTS = {"hanzi": "/System/Library/Fonts/Hiragino Sans GB.ttc", "oracle": "fonts/oracle_bone_script_F5000.ttf",
         "seal": "fonts/seal-scripts/kaiyuan/fonts/KaiyuanSmallSeal-Regular.ttf"}
rows = {}
for line in open("docs/oracle_seal.md"):
    cells = [c.strip() for c in line.split("|")[1:-1]]
    if len(cells) == 8 and cells[0] in SAMPLE:
        rows[cells[0]] = {"hanzi": cells[0], "oracle": cells[2], "seal": cells[3]}
size = 64
img = Image.new("L", (size * 7, size * len(rows)), 255); draw = ImageDraw.Draw(img)
for y, row in enumerate(rows.values()):
    x = 0
    for column, path in FONTS.items():
        draw.text((x, y * size), row[column], font=ImageFont.truetype(path, size - 8), fill=0)
        x += size * (1 if column == "hanzi" else 3)
img.save("probes/oracle_seal_sample.png"); print("rows:", "".join(rows))
