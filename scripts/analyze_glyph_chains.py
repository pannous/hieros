#!/usr/bin/env python3
"""Chain together consecutive-line successor relationships into ordered
sequences - the general form of the P008283 row-group template
(M362+X M269 M106 M009 M206~g M102~e M309~a) reconstructed automatically
instead of by hand.

Two confidence tiers, because a naive greedy walk overstates certainty:

HIGH CONFIDENCE (primary output): only RECIPROCAL edges - A's single
most-frequent successor is B, AND B's single most-frequent predecessor
is A. This is the same standard analyze_residual_collocations.py already
used successfully. Checked against the loose version: only 27 of 162
one-directional "primary" edges are actually reciprocal, and chain count
drops from 106 to 3 - most of the loose chains were an artifact of
always following the single best forward choice without checking whether
the target agrees going backward (a first-order Markov "mode path", not
a validated sequence). The row-group template chain survives this test
completely intact.

LOW CONFIDENCE (kept for reference, clearly separated): the original
greedy one-directional chains. Still real co-occurrence signal, just
not mutually confirmed - treat as exploratory leads, not conclusions.

Twin/slot detection (nodes sharing the same primary predecessor+
successor, substitutable at one position) is run on the RECIPROCAL graph
only, for the same reason.
"""
from __future__ import annotations

import collections

from pe_signs import base_number, code_to_char_map, glyph_for, load_char_to_code, ROOT
from analyze_subheader_syllabary import extract_all_line_code_sequences
from analyze_list_neighbors import per_document_leading_signs

MIN_FREQ = 2
OUT_TSV = ROOT / "texts" / "proto-elamite" / "glyph-chains.tsv"
LOOSE_TSV = ROOT / "texts" / "proto-elamite" / "glyph-chains-loose.tsv"
GLOBAL_TSV = ROOT / "texts" / "proto-elamite" / "glyph-chains-global-order.tsv"


def build_successor_counts(docs: dict[str, list[tuple[str, str]]]) -> collections.Counter:
    counts = collections.Counter()
    for seq in docs.values():
        for i in range(len(seq) - 1):
            a = seq[i][0]
            b = seq[i + 1][0]
            if a != b:
                counts[(a, b)] += 1
    return counts


def primary_edges(succ_counts: collections.Counter) -> tuple[dict, dict]:
    out_edges = collections.defaultdict(collections.Counter)
    in_edges = collections.defaultdict(collections.Counter)
    for (a, b), n in succ_counts.items():
        out_edges[a][b] += n
        in_edges[b][a] += n
    primary_next = {a: ctr.most_common(1)[0] for a, ctr in out_edges.items() if ctr.most_common(1)[0][1] >= MIN_FREQ}
    primary_prev = {b: ctr.most_common(1)[0] for b, ctr in in_edges.items() if ctr.most_common(1)[0][1] >= MIN_FREQ}
    return primary_next, primary_prev


def build_chains(next_map: dict) -> list[list[str]]:
    targets = {b for b, n in next_map.values()}
    heads = [a for a in next_map if a not in targets]
    chains, visited_global = [], set()
    for head in heads:
        if head in visited_global:
            continue
        chain, visited, cur = [head], {head}, head
        while cur in next_map:
            nxt, n = next_map[cur]
            if nxt in visited:
                break
            chain.append(nxt)
            visited.add(nxt)
            cur = nxt
        if len(chain) >= 3:
            chains.append(chain)
            visited_global |= visited
    chains.sort(key=lambda c: -len(c))
    return chains


def slotmates_from(next_map: dict, prev_map: dict) -> dict:
    slot_key = collections.defaultdict(list)
    for node in set(next_map) | set(prev_map):
        p = prev_map.get(node, (None, 0))[0]
        s = next_map.get(node, (None, 0))[0]
        if p is not None or s is not None:
            slot_key[(p, s)].append(node)
    mates = {}
    for (p, s), nodes in slot_key.items():
        if len(nodes) > 1:
            for node in nodes:
                mates[node] = [m for m in nodes if m != node]
    return mates


def write_chains(path, chains: list[list[str]], mates: dict, code2char: dict, label: str) -> None:
    print(f"{len(chains)} {label} chains of length >= 3")
    print()
    with path.open("w", encoding="utf-8") as f:
        f.write("chain_length\tcodes\tglyphs\n")
        for chain in chains:
            cells = ["/".join(glyph_for(g, code2char) for g in [n] + mates.get(n, [])) for n in chain]
            line = " ".join(cells)
            print(line)
            codes_line = " ".join("/".join([n] + mates.get(n, [])) for n in chain)
            f.write(f"{len(chain)}\t{codes_line}\t{line}\n")
    print()
    print(f"wrote {path}")


def main() -> None:
    char2code = load_char_to_code()
    code2char = code_to_char_map(char2code)
    rows = extract_all_line_code_sequences(char2code)
    docs = per_document_leading_signs(rows)
    succ_counts = build_successor_counts(docs)

    primary_next, primary_prev = primary_edges(succ_counts)

    reciprocal_next = {
        a: (b, n) for a, (b, n) in primary_next.items()
        if primary_prev.get(b, (None, 0))[0] == a
    }
    reciprocal_prev = {b: (a, n) for a, (b, n) in reciprocal_next.items()}

    print(f"{len(primary_next)} one-directional primary edges, {len(reciprocal_next)} survive reciprocal check")
    print()

    print("=== HIGH CONFIDENCE: reciprocally-validated chains ===")
    high_mates = slotmates_from(reciprocal_next, reciprocal_prev)
    high_chains = build_chains(reciprocal_next)
    write_chains(OUT_TSV, high_chains, high_mates, code2char, "reciprocally-validated")

    print()
    print("=== LOW CONFIDENCE: one-directional greedy chains (exploratory only) ===")
    loose_mates = slotmates_from(primary_next, primary_prev)
    loose_chains = build_chains(primary_next)
    write_chains(LOOSE_TSV, loose_chains, loose_mates, code2char, "one-directional (unvalidated)")

    # --- Global ordering: pools ALL freq>=MIN_FREQ edges, not just top-1
    # per node, so it's a different (complementary) signal from either
    # chain tier above rather than depending on their reciprocity.
    qualifying_edges = [(a, b, n) for (a, b), n in succ_counts.items() if n >= MIN_FREQ]
    net_flow = collections.Counter()
    for a, b, n in qualifying_edges:
        net_flow[a] += n
        net_flow[b] -= n

    order = sorted(net_flow, key=lambda node: -net_flow[node])
    print()
    print(f"=== global order by net flow (all {len(qualifying_edges)} edges freq>={MIN_FREQ} pooled) ===")
    print("(positive = net 'precedes', negative = net 'is preceded by' - M288 anchors the tail as expected)")
    with GLOBAL_TSV.open("w", encoding="utf-8") as f:
        f.write("rank\tnet_flow\tbase\tglyph\n")
        for rank, node in enumerate(order, 1):
            if rank <= 20 or rank > len(order) - 20:
                print(f"{rank:4d}  net={net_flow[node]:+4d}  {glyph_for(node, code2char)} {node}")
            f.write(f"{rank}\t{net_flow[node]}\t{node}\t{glyph_for(node, code2char)}\n")
    print(f"wrote {GLOBAL_TSV}")


if __name__ == "__main__":
    main()
