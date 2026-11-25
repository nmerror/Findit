# Activating the automatic build and the project page

The two workflow files live here as a **reference copy**, because the GitHub
connection used to push this repository is not allowed to write into
`.github/workflows/`. They are only two clicks away from running.

| File | What it does |
|---|---|
| `ci.yml` → `.github/workflows/ci.yml` | On every push and every pull request: compiles the project, starts a real MySQL 8 in a container, runs the 43 unit tests and the 49 database tests, and takes a screenshot of every screen. |
| `pages.yml` → `.github/workflows/pages.yml` | Publishes the project page with those screenshots on `https://nmerror.github.io/Findit`. |

---

## Step 1 — create the two workflow files (2 minutes)

1. Open the repository on GitHub: **https://github.com/nmerror/Findit**
2. Click **Add file → Create new file**
3. Type the path in the box at the top:
   `.github/workflows/ci.yml`
   (typing the slashes opens the folders automatically)
4. Open **docs/workflows/ci.yml** in this repository, copy **all** of it and paste it
   into the editor on GitHub.
5. Repeat the same for `.github/workflows/pages.yml` with the content of
   **docs/workflows/pages.yml**.
6. For each file: click **Commit changes → Commit changes** (a direct commit to the
   branch is fine).

> **Shorter way:** open `docs/workflows/ci.yml` in the GitHub file view, click the
> **pencil icon** at the top right, then change the path in the breadcrumb to
> `.github/workflows/ci.yml`, commit, and repeat for the second file.

## Step 2 — switch on GitHub Pages (10 seconds)

1. **Settings → Pages**
2. Under **Build and deployment → Source**, choose **GitHub Actions**
3. Save.

## Step 3 — let it run

Open the **Actions** tab. The workflow **Deploy the demo site** starts on its own
(the first push to `main` triggers it, or you can press **Run workflow**).
After about two minutes the project page is live at:

> **https://nmerror.github.io/Findit**

The **Build & Test** workflow runs on every push and every pull request. A green
tick on the pull request is the proof that the code compiles, that the 43 unit
tests pass and that the 49 database tests pass against a real MySQL server.
