#!/usr/bin/env python3
"""Chain together consecutive-line successor relationships into maximal
ordered sequences - the general form of the P008283 row-group template
(M362+X M269 M106 M009 M206~g M102~e M309~a) reconstructed automatically
instead of by hand.

Method: build a directed "what usually comes next" graph from immediate
(gap=1) successor pairs across all documents (base_number level, so
graphic variants of one sign count as one node). Keep only edges with
count >= MIN_FREQ. For each node take its single most-frequent successor
as the "primary" edge; follow primary edges to build maximal chains
(starting from nodes nothing points to, stopping at a node with no
qualifying successor or a revisit). Two nodes with the SAME primary
successor AND the same primary predecessor are "twins" - substitutable
options at the same chain position (this is how M269-family and M260
merge into one slot) - reported as alt1/alt2/... in the chain.
"""
from __future__ import annotations

import collections

from pe_signs import base_number, code_to_char_map, glyph_for, load_char_to_code, ROOT
from analyze_subheader_syllabary import extract_all_line_code_sequences
from analyze_list_neighbors import per_document_leading_signs

MIN_FREQ = 2
OUT_TSV = ROOT / "texts" / "proto-elamite" / "glyph-chains.tsv"


def build_successor_counts(docs: dict[str, list[tuple[str, str]]]) -> collections.Counter:
    counts = collections.Counter()
    for seq in docs.values():
        for i in range(len(seq) - 1):
            a = seq[i][0]
            b = seq[i + 1][0]
            if a != b:
                counts[(a, b)] += 1
    return counts


def main() -> None:
    char2code = load_char_to_code()
    code2char = code_to_char_map(char2code)
    rows = extract_all_line_code_sequences(char2code)
    docs = per_document_leading_signs(rows)

    succ_counts = build_successor_counts(docs)

    # primary successor/predecessor per node (top choice only)
    out_edges = collections.defaultdict(collections.Counter)
    in_edges = collections.defaultdict(collections.Counter)
    for (a, b), n in succ_counts.items():
        out_edges[a][b] += n
        in_edges[b][a] += n

    primary_next = {}
    primary_prev = {}
    for a, ctr in out_edges.items():
        b, n = ctr.most_common(1)[0]
        if n >= MIN_FREQ:
            primary_next[a] = (b, n)
    for b, ctr in in_edges.items():
        a, n = ctr.most_common(1)[0]
        if n >= MIN_FREQ:
            primary_prev[b] = (a, n)

    # twins: nodes sharing the same primary predecessor AND primary successor
    slot_key = collections.defaultdict(list)
    for node in set(primary_next) | set(primary_prev):
        p = primary_prev.get(node, (None, 0))[0]
        s = primary_next.get(node, (None, 0))[0]
        if p is not None or s is not None:
            slot_key[(p, s)].append(node)

    node_to_slotmates = {}
    for (p, s), nodes in slot_key.items():
        if len(nodes) > 1:
            for node in nodes:
                node_to_slotmates[node] = [m for m in nodes if m != node]

    # build chains: start at nodes with no qualifying predecessor (chain heads)
    has_predecessor = {b for a, (b, n) in ((None, primary_next[a]) for a in primary_next)}  # placeholder unused
    successors_set = set(primary_next.keys())
    targets = {b for b, n in primary_next.values()}
    chain_heads = [a for a in primary_next if a not in targets]

    chains = []
    visited_global = set()
    for head in chain_heads:
        if head in visited_global:
            continue
        chain = [head]
        visited = {head}
        cur = head
        while cur in primary_next:
            nxt, n = primary_next[cur]
            if nxt in visited:
                break
            chain.append(nxt)
            visited.add(nxt)
            cur = nxt
        if len(chain) >= 3:  # skip trivial 1-2 node "chains"
            chains.append(chain)
            visited_global |= visited

    chains.sort(key=lambda c: -len(c))

    print(f"{len(chains)} chains of length >= 3 (min edge frequency {MIN_FREQ})")
    print()
    lines_out = []
    for chain in chains:
        cells = []
        for node in chain:
            mates = node_to_slotmates.get(node, [])
            group = [node] + mates
            cells.append("/".join(glyph_for(g, code2char) for g in group))
        line = " ".join(cells)
        print(line)
        codes_line = " ".join("/".join([n] + node_to_slotmates.get(n, [])) for n in chain)
        lines_out.append((len(chain), codes_line, line))

    with OUT_TSV.open("w", encoding="utf-8") as f:
        f.write("chain_length\tcodes\tglyphs\n")
        for length, codes, glyphs in lines_out:
            f.write(f"{length}\t{codes}\t{glyphs}\n")
    print()
    print(f"wrote {OUT_TSV}")

    # --- Global ordering, independent of the greedy local chains above ---
    # The chains only ever follow each node's single BEST successor, so two
    # signs that are locally "independent" (neither is the other's top
    # choice) can still carry a real global precedence signal once ALL
    # freq>=MIN_FREQ edges are pooled, not just the top-1 per node. Score
    # each node by net flow (total outgoing edge weight minus total
    # incoming): a node that mostly PRECEDES things nets positive and
    # ranks early; a node mostly FOLLOWED-INTO (like M288) nets very
    # negative and ranks late - giving one proposed total order across
    # every sign that clears the frequency bar, not just chain fragments.
    qualifying_edges = [(a, b, n) for (a, b), n in succ_counts.items() if n >= MIN_FREQ]
    net_flow = collections.Counter()
    for a, b, n in qualifying_edges:
        net_flow[a] += n
        net_flow[b] -= n

    order = sorted(net_flow, key=lambda node: -net_flow[node])
    print()
    print(f"=== global order by net flow (all {len(qualifying_edges)} edges freq>={MIN_FREQ} pooled) ===")
    print("(positive = net 'precedes', negative = net 'is preceded by' - M288 anchors the tail as expected)")
    GLOBAL_TSV = ROOT / "texts" / "proto-elamite" / "glyph-chains-global-order.tsv"
    with GLOBAL_TSV.open("w", encoding="utf-8") as f:
        f.write("rank\tnet_flow\tbase\tglyph\n")
        for rank, node in enumerate(order, 1):
            if rank <= 20 or rank > len(order) - 20:
                print(f"{rank:4d}  net={net_flow[node]:+4d}  {glyph_for(node, code2char)} {node}")
            f.write(f"{rank}\t{net_flow[node]}\t{node}\t{glyph_for(node, code2char)}\n")
    print(f"wrote {GLOBAL_TSV}")


if __name__ == "__main__":
    main()
