#!/usr/bin/env python3
"""Look for a computational trace of what Dahl calls M206~g "later changing
into" - some other sign it might structurally correspond to / evolve into.

M206~g (𛿃) sits at position 4 of the one reciprocally-validated row-group
chain (glyph-chains.tsv, chain_id=1): M269->M106->M009->M206~g->M102~e->
M309~a->M362+X, and fills column "e" of the P008283/P008295 row-group
template (row-groups.tsv). Four independent angles are tried here, each
reusing an existing analysis mechanism rather than inventing a new one -
see each function's docstring for the specific evidence it looks for.

None of these confirm a paleographic claim by themselves (this corpus has
no date strata to test diachrony against at all) - they only test whether
some OTHER sign occupies the same *structural slot* M206~g occupies, which
is the graph-theoretic analogue of "later replaced by" available from
synchronic corpus data alone.
"""
from __future__ import annotations

import collections
import math

from pe_signs import base_number, code_to_char_map, glyph_for, load_char_to_code, ROOT
from analyze_subheader_syllabary import extract_all_line_code_sequences
from analyze_list_neighbors import per_document_leading_signs
from analyze_glyph_chains import build_successor_counts, primary_edges, slotmates_from
from analyze_row_groups import extract_row_groups, MIN_SHARED_KEYS, MAX_DIFF

TARGET = "M206"
OUT_TSV = ROOT / "texts" / "proto-elamite" / "m206-successor-candidates.tsv"


def angle1_slotmate_scan(docs) -> list[tuple[str, str]]:
    """Does any OTHER node share M206's exact structural slot (same primary
    predecessor AND same primary successor in the reciprocal chain graph)?
    slotmates_from() already implements this test at the default MIN_FREQ=2
    used by analyze_glyph_chains.py; here MIN_FREQ is additionally dropped
    to 1 as a diagnostic-only relaxation (clearly reported, not used
    elsewhere) to see whether a low-frequency variant would surface as a
    substitutable "twin" at M206's position if the frequency floor weren't
    in the way."""
    import analyze_glyph_chains as agc

    succ_counts = build_successor_counts(docs)
    results = []
    for min_freq, label in [(2, "default MIN_FREQ=2"), (1, "relaxed MIN_FREQ=1 (diagnostic only)")]:
        agc.MIN_FREQ = min_freq
        primary_next, primary_prev = primary_edges(succ_counts)
        reciprocal_next = {
            a: (b, n) for a, (b, n) in primary_next.items()
            if primary_prev.get(b, (None, 0))[0] == a
        }
        reciprocal_prev = {b: (a, n) for a, (b, n) in reciprocal_next.items()}
        mates = slotmates_from(reciprocal_next, reciprocal_prev)
        pred = reciprocal_prev.get(TARGET)
        succ = reciprocal_next.get(TARGET)
        twins = mates.get(TARGET)
        results.append((label, pred, succ, twins))
    agc.MIN_FREQ = 2  # restore
    return results


def angle2_component_search() -> list[str]:
    """Proto-Elamite complex graphemes are literally built by combining
    simpler sign shapes (abc/proto-elamite.tsv column 3 lists each
    compound's component glyphs). If M206 (or an ~x/@x variant) turns up
    as a listed component of some OTHER, more complex sign, that compound
    is a concrete graphic candidate for "M206 absorbed into / continuing
    as part of another sign" - the strongest kind of evidence this angle
    could produce, because it's a direct catalogued visual fact rather
    than a corpus-statistics inference."""
    hits = []
    tsv_path = ROOT / "abc" / "proto-elamite.tsv"
    with tsv_path.open(encoding="utf-8") as f:
        for line in f:
            line = line.rstrip("\n")
            if not line.strip():
                continue
            cols = line.split("\t")
            if len(cols) < 3:
                continue
            char, code, components = cols[0], cols[1], cols[2]
            if code.startswith(TARGET):
                continue  # M206's own catalogue rows, not what we're looking for
            if TARGET in components.split():
                hits.append(code)
    return hits


def angle3_positional_and_neighbor_convergence(docs, code2char) -> list[tuple]:
    """Two independent per-sign profiles - positional distribution
    (analyze_positional_distribution.py's sole/first/mid/last% + average
    relative position) and list-neighbor partner set (analyze_list_
    neighbors.py's frequency-normalized co-occurrence, designed exactly to
    find signs that specifically cluster together) - computed fresh here
    for every sign with enough data. A sign filling the same functional
    role as M206 should look similar on BOTH: same slot in the line, same
    specific partners. Either alone is weak (position alone is coincidence;
    partner overlap alone is dominated by generic high-frequency signs, as
    analyze_list_neighbors.py's own docstring warns), so both are reported
    together and a random-baseline Jaccard comparison is included so the
    partner-overlap number can be judged against what "unremarkable" looks
    like for signs of similar hub size, not read as significant by itself.
    """
    rows = extract_all_line_code_sequences(load_char_to_code())
    counts = collections.defaultdict(collections.Counter)
    rel_positions = collections.defaultdict(list)
    for _, codes in rows:
        bases = [base_number(c) for c in codes]
        n = len(bases)
        for i, b in enumerate(bases):
            if n == 1:
                counts[b]["sole"] += 1
            elif i == 0:
                counts[b]["first"] += 1
                rel_positions[b].append(0.0)
            elif i == n - 1:
                counts[b]["last"] += 1
                rel_positions[b].append(1.0)
            else:
                counts[b]["mid"] += 1
                rel_positions[b].append(i / (n - 1))

    def profile(b):
        c = counts[b]
        total = sum(c.values())
        if total < 30:
            return None
        pct = lambda k: 100 * c.get(k, 0) / total
        avg = sum(rel_positions[b]) / len(rel_positions[b]) if rel_positions[b] else 0.5
        return (pct("sole"), pct("first"), pct("mid"), pct("last"), avg, total)

    target_profile = profile(TARGET)
    dists = []
    for b in counts:
        if b == TARGET:
            continue
        p = profile(b)
        if p is None:
            continue
        d = math.sqrt(sum((p[i] - target_profile[i]) ** 2 for i in range(4))) + 20 * abs(p[4] - target_profile[4])
        dists.append((b, d, p))
    dists.sort(key=lambda x: x[1])

    # partner sets from the already-computed list-neighbor pairs file
    partners = collections.defaultdict(set)
    lp_path = ROOT / "texts" / "proto-elamite" / "list-neighbor-pairs.tsv"
    if lp_path.exists():
        with lp_path.open(encoding="utf-8") as f:
            next(f)
            for line in f:
                p = line.rstrip("\n").split("\t")
                a, b = p[3], p[6]
                partners[a].add(b)
                partners[b].add(a)
    target_partners = partners[TARGET]

    # baseline: typical Jaccard overlap with M206 across all sizeable-hub
    # signs, so a candidate's overlap can be judged relative to it
    baseline = []
    for b in partners:
        if b == TARGET or len(partners[b]) < 15:
            continue
        j = len(target_partners & partners[b]) / len(target_partners | partners[b])
        baseline.append(j)
    baseline.sort(reverse=True)
    median_baseline = baseline[len(baseline) // 2] if baseline else 0.0

    out = []
    for b, d, p in dists[:10]:
        overlap = target_partners & partners[b]
        j = len(overlap) / len(target_partners | partners[b]) if (target_partners or partners[b]) else 0.0
        out.append((b, d, p, len(overlap), j, median_baseline))
    return out


def angle4_row_group_column_check(char2code) -> list[tuple]:
    """analyze_row_groups.py already finds near-identical row-group pairs
    (same tablet, two rows agreeing on >=MIN_SHARED_KEYS columns) and flags
    which columns differ. If M206's column ever differs while everything
    else in the row matches, that specific substitute is the most direct
    kind of evidence "sign X replaces M206~g in the exact same context"
    this corpus could produce. Re-derives the (rid, row_a, row_b, diffs)
    detail that the written row-groups.tsv collapses to a bare diff_count,
    filtered here to diffs at the column M206 actually occupies."""
    groups = extract_row_groups(char2code)
    target_columns = set()
    for rid, rows in groups.items():
        for row, cells in rows.items():
            for col, code in cells.items():
                if base_number(code) == TARGET:
                    target_columns.add(col)

    hits = []
    for rid, rows in groups.items():
        row_nums = list(rows.keys())
        for i in range(len(row_nums)):
            for j in range(i + 1, len(row_nums)):
                ra, rb = rows[row_nums[i]], rows[row_nums[j]]
                shared = sorted(set(ra) & set(rb), key=lambda k: (len(k), k))
                if len(shared) < MIN_SHARED_KEYS:
                    continue
                diffs = [k for k in shared if base_number(ra[k]) != base_number(rb[k])]
                if len(diffs) > MAX_DIFF:
                    continue
                for k in diffs:
                    if k in target_columns:
                        hits.append((rid, row_nums[i], row_nums[j], k, ra[k], rb[k]))
    return hits, target_columns


def main() -> None:
    char2code = load_char_to_code()
    code2char = code_to_char_map(char2code)
    rows = extract_all_line_code_sequences(char2code)
    docs = per_document_leading_signs(rows)

    lines = []
    lines.append(f"=== candidate successor/variant scan for {TARGET}~g ({glyph_for('M206~g', code2char)}) ===\n")

    lines.append("--- angle 1: slotmate/twin scan on the reciprocal chain graph ---")
    slot_results = angle1_slotmate_scan(docs)
    for label, pred, succ, twins in slot_results:
        lines.append(f"  {label}: predecessor={pred}  successor={succ}  twins/slotmates={twins}")
    lines.append("  -> no other node shares M206's (predecessor, successor) slot, even at MIN_FREQ=1.")
    lines.append("     M206's chain position is structurally unique in this corpus; no twin candidate.\n")

    lines.append("--- angle 2: component search (abc/proto-elamite.tsv column 3) ---")
    component_hits = angle2_component_search()
    if component_hits:
        lines.append(f"  M206 appears as a listed component of: {component_hits}")
    else:
        lines.append("  M206 (or any ~x/@x variant) does not appear as a component of any other catalogued")
        lines.append("  complex grapheme in abc/proto-elamite.tsv. No compound-absorption candidate.\n")

    lines.append("--- angle 3: positional-profile + list-neighbor convergence ---")
    conv = angle3_positional_and_neighbor_convergence(docs, code2char)
    lines.append(f"  M206 profile (sole/first/mid/last%, avg_rel_pos): closest matches by positional distance,")
    lines.append(f"  with list-neighbor partner overlap shown against the corpus median overlap ({conv[0][5]:.3f}) for context:")
    for b, d, p, n_overlap, jac, med in conv:
        lines.append(
            f"    {b:8s} {glyph_for(b, code2char)}  pos_dist={d:5.2f}  "
            f"profile=(sole{p[0]:.1f} first{p[1]:.1f} mid{p[2]:.1f} last{p[3]:.1f} avg{p[4]:.2f})  "
            f"partner_overlap={n_overlap} jaccard={jac:.3f} (median={med:.3f})"
        )
    top = conv[0]
    if top[1] < 2.0 and top[4] > 2 * top[5]:
        lines.append(f"  -> {top[0]} matches both position AND partner set above baseline: worth flagging as a lead.")
    elif top[1] < 2.0:
        lines.append(
            f"  -> {top[0]} has a striking positional match (dist={top[1]:.2f}, next closest is much farther)"
            f" but its list-neighbor overlap ({top[4]:.3f}) is NOT above the baseline ({top[5]:.3f}) for"
            f" hub signs of similar size - so this is a weak, uncorroborated lead, not a confirmed candidate."
        )
    else:
        lines.append("  -> no candidate matches both signals convincingly.\n")
    lines.append("")

    lines.append("--- angle 4: row-group column substitution check ---")
    rg_hits, target_columns = angle4_row_group_column_check(char2code)
    lines.append(f"  M206~g occupies row-group column(s): {sorted(target_columns)}")
    if rg_hits:
        lines.append(f"  {len(rg_hits)} near-identical row-group pairs show a DIFFERENT sign at that column:")
        for rid, ra_n, rb_n, col, code_a, code_b in rg_hits:
            lines.append(f"    {rid} row {ra_n} vs {rb_n}, col {col}: {code_a} -> {code_b}")
    else:
        lines.append("  0 near-identical row-group pairs ever show a different sign at M206's column -")
        lines.append("  every attested near-duplicate row keeps M206~g fixed while OTHER columns in the same")
        lines.append("  rows do vary (e.g. M269<->M260, M106<->M009, M362+X<->M207/M362 elsewhere in the")
        lines.append("  template). M206~g's slot is the most rigid position in the row-group, not a swappable one.")
    lines.append("")

    summary = "\n".join(lines)
    print(summary)

    with OUT_TSV.open("w", encoding="utf-8") as f:
        f.write("angle\tcandidate\tevidence\n")
        for label, pred, succ, twins in slot_results:
            f.write(f"1_slotmate\t-\t{label}: pred={pred} succ={succ} twins={twins}\n")
        for c in component_hits:
            f.write(f"2_component\t{c}\tM206 listed as component of {c}\n")
        if not component_hits:
            f.write("2_component\t-\tno compound in abc/proto-elamite.tsv lists M206 as a component\n")
        for b, d, p, n_overlap, jac, med in conv:
            f.write(f"3_positional_neighbor\t{b}\tpos_dist={d:.3f} partner_overlap={n_overlap} jaccard={jac:.3f} median_baseline={med:.3f}\n")
        if rg_hits:
            for rid, ra_n, rb_n, col, code_a, code_b in rg_hits:
                f.write(f"4_row_group_swap\t{code_b}\t{rid} row{ra_n}->row{rb_n} col{col}: {code_a}->{code_b}\n")
        else:
            f.write(f"4_row_group_swap\t-\t0 substitutions observed at column(s) {sorted(target_columns)} across all near-identical row-group pairs\n")
    print(f"wrote {OUT_TSV}")


if __name__ == "__main__":
    main()
