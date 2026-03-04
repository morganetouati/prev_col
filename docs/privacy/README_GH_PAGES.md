How to publish the privacy page to GitHub Pages

Option A: Push `docs/` to the `main` branch and enable Pages
1. Create a GitHub repository (or use an existing one) and push this project.
2. In the repo settings → Pages, set the source to `main` branch and `/docs` folder.
3. Wait a minute — your page will be at: https://<your-username>.github.io/<repo>/privacy/

Option B: Publish the `docs` folder to the `gh-pages` branch using `gh` CLI

# Requirements
- `git` installed and configured
- optionally `gh` (GitHub CLI) for convenient publishing

Using `gh` (recommended):
```powershell
# from project root
gh repo create <your-username>/<repo> --public --source=. --remote=origin
git add docs/privacy -f
git commit -m "Add privacy page"
git push origin main
# then enable Pages in the settings or use the web UI
```

Manual deploy to `gh-pages` branch:
```powershell
# from project root
git checkout --orphan gh-pages
git --work-tree=docs/privacy add --all
git --work-tree=docs/privacy commit -m "Deploy privacy page"
git push origin HEAD:gh-pages --force
git checkout -
```

After publishing, set the Play Console privacy URL to:
https://<your-username>.github.io/<repo>/privacy/
