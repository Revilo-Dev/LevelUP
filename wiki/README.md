# Wiki Source

This folder is the source of truth for the GitHub wiki content for `LevelUP`.

GitHub wikis are stored in a separate repository ending in `.wiki.git`, so GitHub does not render files from this folder automatically. The workflow in `.github/workflows/wiki-sync.yml` publishes this folder to the repository wiki.

## Conventions

- `Home.md` becomes the wiki home page.
- `_Sidebar.md` becomes the wiki sidebar.
- Every other `*.md` file becomes a wiki page.
- Use standard GitHub Markdown links between pages, for example `[Getting Started](Getting-Started)`.

## One-time setup

1. Enable the repository wiki in GitHub repository settings.
2. Create the first wiki page on GitHub so the `.wiki.git` repository exists.
3. Create a repository secret named `WIKI_PUSH_TOKEN`.
4. Set that secret to a GitHub token that can push to `Revilo-Dev/LevelUP.wiki.git`.

## Publishing

Push changes to `wiki/**` on `main`, or run the `Sync Wiki` workflow manually.
