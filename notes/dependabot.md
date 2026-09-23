# Dependabot automation (hieros)

- `.github/workflows/dependabot-automerge.yml`: runs `npm test` in neolithic-map on every PR touching it; Dependabot PRs that pass and are not semver-major get merged automatically.
- The repo allows merge commits only, so `gh pr merge --squash` fails. Use `--merge`.
- Semver-major bumps and alerts that need `npm audit fix --force` (esbuild, image-size, react-server-dom-webpack as of 2026-09-23) still need a person to review them.
- To get existing Dependabot PRs to run the workflow, comment `@dependabot rebase` on them.

# npm audit fix across repos

- Reusable workflow: `.github/workflows/npm-audit-fix.yml` in hieros. It runs weekly (Mondays 04:17 UTC) and can also be started by hand.
- Other repos get a small caller workflow from `~/dev/bin/install-npm-audit-fix owner/repo...`. Installed so far: warp, Listen, remote.
- Deliberately left out: jobs (client work) and redox-base/redox-rust (lockfiles are vendored upstream rust-analyzer code).
- CI installs npm@latest. Node 22's bundled npm 10 removed the `libc` fields that npm 11 writes into lockfiles, which made the lockfile flip-flop between CI and local runs.
- A lockfile without a package.json next to it (hieros/dicts) is skipped with a warning. Delete it or add a package.json.
- remote still has 2 critical and 1 high vulnerability that need `--force` upgrades (request, websocket).
