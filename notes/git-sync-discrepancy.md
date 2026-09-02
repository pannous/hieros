# The "out of sync" GitHub chip — resolved

## The number itself: the chip accumulates instead of replacing
Two consecutive readings, with the arithmetic checked against git:

    reading 1:  +202,135  -0
    reading 2:  +317,666  -4,113

    git diff --shortstat 22a52b0c   ->  115,531 insertions(+), 4,113 deletions(-)

    202,135 + 115,531 = 317,666   exact
          0 +   4,113 =   4,113   exact

So reading 2 is reading 1 *plus* the current working-tree diff against the
session base commit — the chip sums successive snapshots rather than showing
the latest one. That is a Claude Code display bug, not a git state problem.
No single git comparison in this repo produces 317,666: every commit in the
last 30 was tested as a base, as were the docs submodule diff (17,466/4,361)
and the untracked-file line count (5,110,845). Only the sum matches.

The deletions being 0 on the first reading is the tell: a fresh counter
starting from a moment when nothing had been deleted yet.

## What it actually was
Two unrelated causes, neither a real divergence from origin:

1. **A concurrent Claude/Codex session commits into the same working copy.**
   During one short investigation HEAD moved 032445e6 -> 7a6d19ba -> e1e08631
   without this session touching anything. The desktop app's diff chip
   (`+N -0`, "Create PR") is a snapshot; it is stale the moment another
   session commits. `git rev-list --left-right --count origin/master...master`
   was `0 0` the whole time — master was never actually out of sync.

2. **Self-contradicting git config** (fixed in db648dac):
   - `.gitmodules` declared submodule `wikis` on the *same path* as `docs`
     (leftover `.git/modules/wikis` still on disk, harmless but dead).
   - `.gitmodules` declared `swadesh` as a submodule, but the index holds it
     as mode `120000` — a symlink to `dicts/swadesh`.
   - `.gitignore` ignored `docs/` while `docs` is a tracked gitlink.
   Tools that read `.gitmodules` naively (IDE/app git panels) choke on these.

## Still present, deliberately not touched
- `.git/modules/wikis` — dead submodule store; safe to delete manually.
- Stale worktree: `/Users/me/.codex/worktrees/40f2/uruk_egypt.nosync`
  (detached at 95b51007, ~93k lines from master). `git worktree list` shows it.
- Stale `heroku` remote, ~4.7M lines from master.
- 11 nested non-submodule git repos (isoglosses, library, dicts/strongs,
  texts/Bibles, opt/oracc...) — several have uncommitted work of their own.

## How to check for real, in one line
    git rev-list --left-right --count origin/master...master
