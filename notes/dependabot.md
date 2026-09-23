# Dependabot automation (hieros)

- `.github/workflows/dependabot-automerge.yml`: runs `npm test` in neolithic-map on every PR touching it; Dependabot PRs that pass and are not semver-major get merged automatically.
- The repo allows merge commits only, so `gh pr merge --squash` fails. Use `--merge`.
- Semver-major bumps and alerts that need `npm audit fix --force` (esbuild, image-size, react-server-dom-webpack as of 2026-09-23) still need a person to review them.
- To get existing Dependabot PRs to run the workflow, comment `@dependabot rebase` on them.
