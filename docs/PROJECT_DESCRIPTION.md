# FindIt — Campus Lost & Found Management System
### The complete project description

> **Course:** Advanced Programming Lab (CSE 342) · BUBT, Department of Computer Science and Engineering
> **Type:** Desktop application (GUI based) · **Language:** Java 17 · **GUI:** Java Swing · **Database:** MySQL 8 via JDBC
> **Repository:** https://github.com/nmerror/Findit

---

## 1. What the project is

FindIt is the software of a **campus lost and found office**. Every day students lose wallets,
phones, ID cards and notebooks in the classrooms, the library, the canteen and on the playing field.
Until now the office solved that with a paper register and a notice board: a student writes his
name on a sheet, and if nobody comes to collect the item within a few weeks it is thrown away.

FindIt turns that register into a database and that notice board into a dashboard. A student
reports what he lost or what he found, the system **matches** the two sides automatically, the
office **verifies** the claim with the photo evidence, and the item goes back to its owner — with a
complete record of who did what.

The application is a **desktop program** written in Java with Swing. It is not a website and it does
not need a server: the four segments of the team develop it together, integrate it into one
executable program, and demonstrate it on one laptop.

### The intended users

| User | What they do in FindIt |
|---|---|
| **Student** | Registers, signs in, reports a lost or found item, follows the claim of their own item, searches for their belongings |
| **Administrator (office staff)** | Sees the whole system, verifies the claims, approves or rejects them, confirms the return, manages the accounts, reads the audit trail and the report |
| **Developer of segments 2/3/4** | Implements one interface of `core/spi` and registers it — no changes in the code of Segment 1 |

---

## 2. The problem it solves

| The manual process | What FindIt does instead |
|---|---|
| The register is a paper sheet — it is lost, it smudges, it is searched by eye | Every report is a row in MySQL, searchable in milliseconds |
| A lost wallet and a found wallet are matched by the memory of one staff member | The matching engine compares category, colour, brand, location and time and ranks the pairs |
| Nobody can prove that the person claiming the phone is its owner | The claim needs photo evidence, and an administrator verifies it against the record |
| Items that nobody claims disappear without a trace | Every item keeps its status — open, claimed, approved, returned — until it is closed |
| There is no record of who did what | Every action is written to the audit log with the user, the time and the details |
| Only the office knows the situation | The student sees the status of his own report in the dashboard |

**Why a Java application solves it:** the data has to survive a restart and to be queried by
relations — a lost item must be connected to its owner, to its location, to its photo and to its
claim. That is a relational database, and MySQL with JDBC is the technology the course asks for.
Swing gives a desktop program that runs on the machine of the office without installing a server,
and Java's class model is what makes it possible for four people to develop four parts in
parallel without stepping on each other.

---

## 3. The objectives

1. **A secure, role based login.** Students and administrators sign in with their e-mail or their
   student ID; passwords are never stored in plain text, sessions expire, and every protected
   action is checked against a permission — in the service layer, not in the button handler.
2. **A complete database for the whole project.** One MySQL schema with eight tables, foreign
   keys and indexes, written once by the team and owned table by table, so that the segments can
   be developed in parallel without writing into each other's data.
3. **A clear path from a lost item back to its owner.** Report → match → claim → verify → return,
   with every step visible in the interface and written to the audit log.
4. **A working, documented and demonstrable desktop application.** Four screens, one executable,
   a test suite that proves the database works against a real MySQL, and documentation that lets a
   new team member join in a day.

---

## 4. The features

| Module | Owner | What it does |
|---|---|---|
| **User login and authentication** | Segment 1 | Registration and sign-in, PBKDF2 hashing with a salt, sessions with an expiry, role based access control, the audit trail |
| **Module 1 — item reporting** | Segment 2 | Report a lost or found item (category, brand, colour, location, description, photo), browse and filter the open reports |
| **Module 2 — intelligent matching** | Segment 3 | Compare the lost and the found reports, score the similarity, propose the pairs that belong together |
| **Module 3 — claims and verification** | Segment 3 & 4 | Claim an item, check the photo evidence, approve or reject the claim, confirm the return to the owner |
| **Search and report generation** | Segment 1 | One global search over people, items and claims; the report of the whole system as a text file or as CSV |

### The screens (section 7 of the proposal)

| # | Screen | What it does |
|---|---|---|
| 1 | **Login / Registration** | The login card (e-mail or student ID + password) with inline validation, and the registration form with the password policy |
| 2 | **Dashboard / Main Menu** | Four KPI cards (lost, found, open claims, users), the claim pipeline, the busiest locations, the table of the recent reports |
| 3 | **Data Management** | The list of the accounts with suspend, restore and role change — administrator only |
| 4 | **Search / Report** | The global search and the report of the system with the export as `.txt` and as `.csv` |
| + | **Profile** and **Audit Log** | The own account, the password change, the activity feed; and the complete trail of every action |

---

## 5. The architecture

```
        ┌──────────────────────────────────────────────────────────┐
        │  ui/            Swing screens (login, dashboard, users,  │
        │                 profile, audit, search, reports)        │
        ├──────────────────────────────────────────────────────────┤
        │  service/       business rules: AuthService, RbacService,│
        │                 SearchService, ReportService, Dashboard… │
        ├───────────────────────────┬──────────────────────────────┤
        │  core/spi/  (contracts)   │  core/event/  EventBus       │
        │  7 interfaces + stubs     │  the segments talk to each    │
        │  ← implemented by S2/S3/S4 │  other through events        │
        ├───────────────────────────┴──────────────────────────────┤
        │  db/  ConnectionPool → Database → dao/ (8 tables)        │
        └───────────────────────────┬──────────────────────────────┘
                                    │  JDBC / PreparedStatement
                            ┌───────┴────────┐
                            │   MySQL 8      │
                            │  8 tables      │
                            └────────────────┘
```

### The package map (104 files, about 9 400 lines)

| Package | Files | Lines | What is inside |
|---|---:|---:|---|
| `core/model` | 13 | 861 | `User`, `Role`, `Permission`, `UserStatus`, `Session`, `OperationResult`, `DashboardStats`, `ItemSummary`, `ItemType`, `ItemCategory`, `ClaimSummary`, `LocationCount` |
| `core/spi` | 7 | 224 | The seven contracts of the segments + `SearchContributor` |
| `core/spi/stub` | 5 | 184 | Temporary implementations so the application always starts |
| `core/event` | 4 | 187 | `AppEvent`, `EventBus`, `EventListener`, `Events` |
| `core/registry` | 1 | 109 | `ServiceRegistry` — the wiring hub |
| `core/util` | 2 | 132 | `PasswordHasher` (PBKDF2), `Validation` |
| `db` | 3 | 464 | `ConnectionPool`, `Database`, `DaoFactory` |
| `db/dao` | 20 | 1 589 | The DAO interface + the JDBC and the in-memory implementation of every table |
| `service` | 9 | 971 | The business rules |
| `service/data` | 5 | 251 | The database backed reference implementations of the contracts |
| `ui` | 28 | 3 653 | The screens and the small design system |
| `app`, `config`, `tools` | 5 | 617 | `AppServices` (the container), `AppConfig`, `Main`, the screenshot and the demo tools |

### The three rules that keep four people out of a merge conflict

1. **A segment only writes its own tables.** Segment 1 owns `users` and `audit_log`; Segment 2 owns
   `locations`, `items` and `item_images`; Segment 3 owns `matches` and `claims`; Segment 4 changes
   the status of a claim. Everything else is read through the foreign keys.
2. **Segment 1 never imports a class of Segment 2, 3 or 4.** It only uses the interfaces of
   `core/spi`. The implementations arrive at run time through the `ServiceRegistry`.
3. **Nobody asks "who is this?" twice.** `AuthContext.currentUser()` is the single source of the
   current user for the whole application.

### How a teammate plugs his segment in

```java
// one line, in his own bootstrap — never inside Segment 1
ServiceRegistry.register(ItemCatalogService.class, new ItemCatalogServiceImpl());
```

and his screen goes into the window with one call:

```java
mainFrame.addSegmentSection("Segment 2 - Reporting", "Report an Item",
        "s2-report", new ReportItemPanel(services));
```

The window creates the entry in the navigation, registers the panel and keeps track of the title.
Nothing else has to be re-plumbed — that is the whole point of the contract layer.

---

## 6. The database

Eight tables, created by `sql/01_schema.sql`, filled with the demo data by `sql/02_seed_data.sql`.

| Table | Purpose | Primary key | Owner |
|---|---|---|---|
| `users` | The accounts: name, e-mail, student ID, hashed password, role, status | `user_id` | Segment 1 |
| `audit_log` | Every action: user, action, timestamp, details | `audit_id` | Segment 1 |
| `notifications` | The alerts of the dashboard | `notification_id` | Segment 1 |
| `locations` | The places of the campus | `location_id` | Segment 2 |
| `items` | The lost and the found reports | `item_id` | Segment 2 |
| `item_images` | The photo of a report | `image_id` | Segment 2 |
| `matches` | The pairs that the matching engine proposes | `match_id` | Segment 3 |
| `claims` | The claims and their verification | `claim_id` | Segment 3 & 4 |

**The relations:** `items.reported_by → users.user_id`, `items.location_id → locations.location_id`,
`item_images.item_id → items.item_id`, `matches.lost_item_id` / `matches.found_item_id → items.item_id`,
`claims.item_id → items.item_id`, `claims.claimant_id → users.user_id`, `claims.match_id → matches.match_id`,
`notifications.user_id → users.user_id`.

**The JDBC work** — all six operations of section 9 of the proposal are used:

| Operation | Where |
|---|---|
| `INSERT` | `JdbcUserDao.create`, `JdbcItemDao.insert`, `JdbcClaimDao.insert`, … |
| `SELECT` | every `findBy…` method of the DAOs |
| `UPDATE` | `JdbcUserDao.updateRole`, `updateStatus`, `JdbcClaimDao.updateStatus` |
| `DELETE` | `JdbcUserDao.deleteById`, `JdbcAuditDao.deleteOlderThan` |
| `SEARCH / FILTER` | `SearchService` and the filtered queries of the dashboard |
| `JOIN / REPORT` | the report of `ReportService` (users, items, claims and audit log in one query) |

Every statement is a `PreparedStatement` with `?` placeholders — **no value is ever glued into a
query string**, so the application is safe against SQL injection. The connection handling is in
`db/`: a pool of eight connections hands a connection out and takes it back, and
`Database.inTransaction(...)` commits on success and rolls back on failure.

---

## 7. The Java OOP concepts (section 8 of the proposal)

| Concept | Where it is in the code |
|---|---|
| **Classes and objects** | `User`, `Role`, `Session`, `OperationResult`, `DashboardStats`, `ItemSummary`, `ClaimSummary` — the whole domain model of `core/model` |
| **Encapsulation** | private fields with getters and setters; `User.sanitized()` returns a copy without the password hash, so the hash never leaves the object |
| **Inheritance** | `ui/common/BaseFrame` is the **abstract** base class of every window; `LoginFrame`, `RegisterFrame` and `MainFrame` **extend** it and get the title, the size and the content panel from it |
| **Polymorphism** | *overriding*: every DAO interface has two implementations — `JdbcUserDao` (MySQL) and `InMemoryUserDao` (demo) — and the application asks for the interface, never for the class; *overloading*: `PasswordHasher.hash(...)`, `ServiceRegistry.register(...)` |
| **Abstraction** | the seven **interfaces** of `core/spi`, the **abstract class** `BaseFrame`, the DAO interfaces in `db/dao` — the application depends on the contract, not on the implementation |
| **Exception handling** | `try/catch` in the DAOs, the custom exceptions `DataAccessException` and `AccessDeniedException`, and `OperationResult` instead of exceptions across the interface — a failed login is a result, not a crash |
| **Collections** | `ArrayList`, `HashMap`, `ConcurrentHashMap`, `CopyOnWriteArrayList`, `List.of()` in `SearchService`, `ServiceRegistry` and `EventBus` |
| **Event handling** | `ActionListener` on the buttons, `DocumentListener` for the live search while typing, `FocusListener` for the input fields, `MouseListener` for the links, `WindowAdapter` for the windows — and the own `EventBus` for the communication between the segments |

---

## 8. The tools and the techniques

| Tool | Version | Purpose |
|---|---|---|
| JDK (Temurin) | 17 | The application itself; `setup-java.sh` installs it into the project if the machine has none |
| Java Swing | part of the JDK | The whole user interface |
| MySQL | 8.0 | The persistent storage |
| JDBC driver `mysql-connector-j` | 8.4.0 | The connection to MySQL |
| IntelliJ IDEA / VS Code | — | Coding and debugging |
| Git + GitHub | — | The source control, the pull requests and the automatic build |
| GitHub Actions | — | The continuous integration: compile, unit tests, database tests against a real MySQL, screenshots |
| GitHub Pages | — | The public project page with the screenshots of every screen |

---

## 9. How it is built and tested

| Check | How | Result |
|---|---|---|
| The unit tests | `java -cp target/classes:target/test-classes com.bubt.findit.SelfTest` | **43 / 43** |
| The database tests | `java -cp "target/classes:target/test-classes:lib/*" com.bubt.findit.BackendSelfTest` against a live MySQL | **49 / 49** |
| The automatic build | GitHub Actions on every push and every pull request — compiles, tests, takes the screenshots | green tick on the pull request |
| The clean check-out | `git clone` + `bash setup-java.sh` + `bash run.sh` — the application starts with no exception | verified |

The test suite is the safety net of the team: after **any** change in `db/` the 49 database tests
have to run against a real MySQL, because the in-memory mode hides exactly the mistakes that
matter (a wrong column name, a broken join, a transaction that does not roll back).

---

## 10. The expected outcome

* The office stops losing the register: every report, claim and decision is a row in MySQL.
* A lost wallet comes back to its owner in days instead of being thrown away after a week.
* The matching engine finds the pairs that the staff used to find by memory — and shows its score,
  so a human makes the final decision.
* Every decision of the office is traceable: who suspended an account, who approved a claim, who
  confirmed a return — with the time and the details.
* The student sees the status of his own report without asking anybody.
* The four segments are one program, and the integration works because everybody coded against a
  contract and not against a class.

---

## 11. The project structure

```
findit/
├── .github/workflows/ci.yml        ← the automatic build and test
├── .github/workflows/pages.yml     ← the project page with the screenshots
├── sql/01_schema.sql               ← the database of the whole project
├── sql/02_seed_data.sql            ← the demo data
├── docs/                           ← the design, the contract, the description
├── docs/site/                      ← the project page (GitHub Pages)
├── run.sh / run.bat                ← compile and start
├── setup-java.sh / setup-java.bat  ← install a private JDK if the machine has none
└── src/main/java/com/bubt/findit/
    ├── core/     model, spi, event, registry, util, AuthContext
    ├── db/       ConnectionPool, Database, DaoFactory, dao/
    ├── service/  the business rules, data/ (the reference implementations)
    ├── ui/       the screens and the design system
    ├── app/      AppServices — the container
    ├── config/   AppConfig
    └── Main.java
```

---

## 12. How to run it

### The demo — no database server needed

```bash
git clone https://github.com/nmerror/Findit.git
cd Findit
bash setup-java.sh     # only if Java is not installed
bash run.sh
```

### With a real MySQL

```bash
mysql -u root -p < sql/01_schema.sql
mysql -u root -p < sql/02_seed_data.sql
# put the credentials in app.properties
bash run.sh --mysql
```

**Log in:** `admin@findit.edu` / `Admin@1234` (administrator) or `ayesha@buet.edu` / `Student@123`
(student).

---

## 13. Where the documentation lives

| Document | What is inside |
|---|---|
| `docs/PROJECT_DESCRIPTION.md` | This document |
| `docs/SEGMENT1_DESIGN.md` | How Segment 1 is designed, class by class |
| `docs/BACKEND_DESIGN.md` | The connection pool, the transactions, the DAOs |
| `docs/INTEGRATION_CONTRACT.md` | What the other segments have to implement — the contract |
| `docs/PROPOSAL_TRACEABILITY.md` | Which line of the proposal is implemented where |
| `docs/SEGMENT1_FILE_INDEX.md` | Every file of the project and what it does |
| `docs/PRESENTATION_SCRIPT.md` | Who presents which part, minute by minute |
| `docs/UI_SIMPLIFICATION_PLAN.md` | The plan of the interface and what was actually done |
