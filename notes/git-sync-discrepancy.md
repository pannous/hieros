# The "out of sync" GitHub chip — resolved

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
