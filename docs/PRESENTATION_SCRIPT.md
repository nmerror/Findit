# Presentation & Viva Plan — FindIt

> **Who presents what, in which order, and what has to be ready for the questions.**
> Total time: 15 minutes of talking + 5 minutes of live demo. Keep a copy of this
> file next to you during the viva.

---

## 0. The order of the whole thing

| Time | Who | What | Slides |
|---|---|---|---|
| 0:00 – 1:30 | **Member 1** — Nurul Azom Arif (Leader) | The introduction: what FindIt is, who uses it, the problem, the objectives | 1 – 3 |
| 1:30 – 2:30 | **Member 1** | The architecture, the split of the four segments, the database plan | 4 – 5 |
| 2:30 – 6:00 | **Member 4** — Sadikul Basar | **Segment 1**: the login, the roles and permissions, the dashboard, the search and the report | 6 – 9 |
| 6:00 – 8:00 | **Member 2** — Muntasir Al Simum | **Segment 2**: report a lost or found item, the locations, the photo, browsing and filtering | 10 – 11 |
| 8:00 – 10:00 | **Member 3** — Rajmohan Chandra Shil | **Segments 3 & 4**: the matching engine, the claims, the verification and the return | 12 – 13 |
| 10:00 – 12:00 | **Member 4** | The technical part: the database, the JDBC work, the connection pool, the tests, the automatic build | 14 – 16 |
| 12:00 – 12:30 | **Member 1** | The expected outcome and the closing | 17 |
| 12:30 – 17:30 | **Member 4** (or the person who is most comfortable) | **The live demo** — see the script in section 2 | — |
| 17:30 – | All four | The questions | — |

**Why this order:** the leader opens (the problem sells the project), the segments are explained in
the order of the data flow (report → match → claim → return), the database and the tests come
after the screens (so the teacher already knows what the program does before hearing how it is
stored), and the demo comes last, when the whole system is fresh.

---

## 1. What each member says

### Member 1 — MD. Nurul Azom Arif (20255103126) — *Leader*
**Your slides: 1 – 5 and 17.** You have the first and the last word.

**Slide 1 — the title.**
> "Good morning. We are team four of CSE 342, and our project is **FindIt, the Campus Lost and
> Found Management System** — a desktop application for the lost and found office of the campus."

**Slide 2 — the problem.** Say it as a story, not as a list:
> "Every day, students lose things in the classrooms, the library and the canteen. Today the office
> solves this with a paper register and a notice board. The register is searched by eye, the
> matching depends on the memory of one staff member, and after a few weeks the unclaimed items are
> thrown away. Nobody can prove that the person claiming a phone is its owner, and there is no record
> of who decided what."

**Slide 3 — the solution and the objectives.**
> "FindIt replaces the register with a database and the notice board with a dashboard. A student
> reports what he lost or found, the system matches the two sides and shows the score, the office
> verifies the claim with the photo evidence, and the item returns to its owner — with every step
> recorded. Our four objectives are: a secure role based login, one database for the whole project,
> a clear path from a lost item back to its owner, and a working and documented application."

**Slide 4 — the architecture.** One sentence, then let the picture speak:
> "The program has four layers: the Swing screens, the service layer with the business rules, the
> contract layer that connects the segments, and the JDBC layer over MySQL. The important idea is
> in the middle: Segment 1 declares the interfaces, the other segments implement them and register
> them at run time. That is why four people could develop four parts in parallel."

**Slide 5 — the split of the team and the database.**
> "Each of us owns a part and a set of tables — I take the coordination and the front, Muntasir the
> item reporting, Rajmohan the matching and the verification, and I the authentication and the
> dashboard. The database has eight tables with foreign keys; no segment ever writes into the
> tables of another one."

**Slide 17 — the closing.**
> "The outcome we expect: the office keeps its register in the database, a lost wallet comes back to
> its owner in days, the matching engine finds what the staff used to find by memory, and every
> decision is traceable. Thank you — we would like to show it running."

---

### Member 4 — Sadikul Basar (20255103159) — *Segment 1: authentication, RBAC, dashboard, search & report*
**Your slides: 6 – 9 and 14 – 16.** You have the biggest technical part.

**Slide 6 — the login.**
> "FindIt starts with the login screen. The identifier accepts the e-mail or the student ID, so the
> student does not have to remember which one he registered with. The password is never stored as
> text — we hash it with PBKDF2 with a random salt and compare the hashes. A wrong password gives
> a message under the field, not a crash, and the session is closed when the window is closed."

**Slide 7 — the roles and the permissions.**
> "There are two roles: student and administrator, and eleven permissions. The important part: the
> permission check lives in the service layer, not in the button. If a student calls an
> administrative operation directly, the service refuses it — the interface only *hides* what the
> user may not do, it does not *prevent* it. And every action of every user is written to the audit
> log with the time and the details."

**Slide 8 — the dashboard.**
> "The dashboard is the main menu. The four cards are live numbers from the database — the lost
> items, the found items, the open claims and the accounts. The claim pipeline shows how the claims
> are distributed, the busiest locations show where things get lost, and the table below lists the
> most recent reports. The numbers are real: they come from the database through the contracts of
> the segments, and the four segments fill this screen automatically once they register."

**Slide 9 — the search and the report.**
> "One search box over people, items and claims — the segments join in through the same contract.
> The report prints the whole system as a text file or as CSV, with the numbers of the users, the
> items, the claims and the audit log."

**Slide 14 — the database.**
> "Eight tables: users, audit log, notifications, locations, items, item images, matches and claims.
> Every statement is a PreparedStatement with question marks — the value is never glued into the
> query, so the application is safe against SQL injection. We use all six operations of the
> proposal: insert, select, update, delete, search and the join for the report."

**Slide 15 — the connection pool and the transactions.**
> "Two things we had to fix. First: every query opened its own TCP connection to MySQL. Now a pool
> of eight connections hands a connection out and takes it back. Second: there was no way to run
> several statements in one transaction — now `inTransaction` commits on success and rolls back on
> failure, and there is a test that proves the rollback."

**Slide 16 — the tests and the automatic build.**
> "We have two test suites: 43 checks that run without a database, and 49 checks that run against a
> real MySQL. Both run automatically on GitHub on every push and every pull request — the green
> tick on the pull request is the proof that the code works. That is also how the screenshots of
> this presentation are produced: the build server starts the application and takes them."

---

### Member 2 — Muntasir Al Simum (20255103134) — *Segment 2: item reporting, locations, photos*
**Your slides: 10 – 11.**

**Slide 10 — reporting an item.**
> "A student reports what he lost or what he found: the category, the brand, the colour, the place on
> campus and a description, and — the important part — a photo. The photo is not decoration: it is
> the evidence that the office checks when it verifies a claim. The places of the campus come from
> the locations table, so the dropdown always matches what the office knows."

**Slide 11 — browsing and filtering.**
> "The browse screen lists the open reports and filters them by type, by category and by location.
> The list is fed by the ItemCatalogService contract, the same contract the dashboard reads from —
> so when I change my implementation, the dashboard changes with it, without touching the code of
> Segment 1."

**Be ready for:** *Why a photo and not only a description?* — because the office has to decide
whether the claimant really owns the item, and a picture of the lost wallet is the only evidence
that survives a week.

---

### Member 3 — Rajmohan Chandra Shil (20255103149) — *Segments 3 & 4: matching, claims, verification*
**Your slides: 12 – 13.**

**Slide 12 — the matching.**
> "The engine compares the lost reports with the found reports: the category, the brand, the colour,
> the place and the time. Every pair gets a similarity score, and the pairs are sorted by that
> score. The score is a suggestion, not a decision — a human always decides. The engine works on
> the rows of the database, so it can be re-run over the whole data set."

**Slide 13 — the claim, the verification and the return.**
> "The student claims an item, and the claim goes into the office queue. The administrator sees the
> claim next to the photo of the item, compares them and approves or rejects it. Every decision is
> written to the audit log and the student gets a notification. When the item is physically handed
> over, the administrator confirms the return and the case is closed — the status of the item goes
> from open to claimed, to approved, to returned."

**Be ready for:** *What if two students claim the same item?* — the office sees both claims at the
same time in the queue and decides; the system refuses a second *approved* claim on the same item.

---

## 2. The live demo — what to click, in this order

Do it on the **administrator** account first, then switch to the student to show the difference.

| # | Action | What you say | What it proves |
|---|---|---|---|
| 1 | Open the application | "This is the real program, running from the database." | It is not a slideshow |
| 2 | Sign in as `admin@findit.edu` / `Admin@1234` | "The password is hashed and compared." | The authentication |
| 3 | The dashboard | "Four live numbers, the pipeline, the locations, the recent reports." | The database + the contracts |
| 4 | Click **Search Results**, type "ayesha" | "One search over people, items and claims." | The global search |
| 5 | Click **Manage Users**, suspend a test account | "Only the administrator sees this — and the service checks it again." | The RBAC |
| 6 | Click **Audit Log** | "Every action of the last minutes is here — including the one I just did." | The audit trail |
| 7 | Click **Reports**, then **Export as .csv** | "The report of the whole system in one file." | The reporting |
| 8 | Sign out, sign in as `ayesha@buet.edu` / `Student@123` | "Notice: no user management, no audit log — the student account is smaller." | The role based navigation |
| 9 | **My Profile** | "The student changes his own data and sees his own activity." | The profile screen |
| 10 | Back to the GitHub tab | "Here is the pull request — the build server compiled the code, ran the 43 and the 49 tests and took these screenshots." | The continuous integration |

> **If something goes wrong:** do not debug live. Say "this is the running version" and go back to
> the slides — the GitHub tab is the backup, it shows the same screens and the green build.

---

## 3. The questions you should be ready for

| Question | The answer in one sentence |
|---|---|
| Why a desktop application and not a website? | The office runs it on one machine, there is no server to maintain, and Swing is what the course asks for. |
| Why is the password hashed and not encrypted? | A hash cannot be reversed — we can check a password but not read it. PBKDF2 with a salt makes the attack expensive. |
| Where is the role check done? | In the service layer — the interface only hides what the user may not do. |
| What happens if two students claim the same item? | Both claims reach the office queue; only one can be approved, and the item goes open → claimed → approved → returned. |
| Why does the database have a pool? | Because opening a TCP connection per query is slow — the pool hands out eight connections and takes them back. |
| What is a `PreparedStatement` and why does it matter? | The query and the values are sent separately, so a value can never become part of the SQL — that is SQL injection protection. |
| How do the four segments meet without conflicts? | Through the contracts in `core/spi`: Segment 1 declares the interfaces, the others implement them and register them at run time. |
| What if a segment is not finished? | The contract has a temporary implementation, so the application always starts and the screen shows what is still missing. |
| How do you know the code works? | 43 unit checks and 49 checks against a real MySQL, running automatically on every pull request. |
| What would you do next? | The matching engine with a real similarity function, photo upload to the server instead of the local disk, and an export of the report as PDF. |

---

## 4. Before the presentation — the checklist

- [ ] The demo database is loaded: `sql/01_schema.sql` **and** `sql/02_seed_data.sql`
- [ ] The passwords of the demo accounts are changed back to the known ones
- [ ] `bash run.sh --mysql` starts without an exception, and the dashboard shows real numbers
- [ ] The laptop is charged, the screen resolution is at least 1400 × 900, and the font is readable
- [ ] The GitHub tab is open on the pull request with the green build (the backup for the demo)
- [ ] Each member has read their own part of this file out loud **once** with a timer
- [ ] A copy of the proposal and of the file index is on the table, in case the teacher asks
