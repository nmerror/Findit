# START HERE — Your Plan for Segment 1

You own **Segment 1: User Authentication, Role-Based Access Control & Dashboard
Management**. This file is the order in which to build it, with the reason each
step comes where it does.

Everything referenced here already exists in this repository — this is the
**working reference implementation**, not a mock-up. Read the file, change it,
make it yours.

---

## The 30-second answer

| Your question | Answer |
|---|---|
| How do I start? | Work through the 8 steps below in order. Steps 1–5 are the foundation; steps 6–7 are the screens. |
| How do I connect with the others? | Through `core/spi` (interfaces they implement), `core/event` (events they listen to), `sql/01_schema.sql` (shared tables) and `users.user_id` (the real join). |
| How do I design Segment 1 so it connects easily? | Segment 1 owns the **shared nouns** (User, Role, Permission, Session) and the **contracts** (SPI + events). It never imports a class from another segment and never writes into another segment's table. |
| What do I hand my teammates? | `docs/INTEGRATION_CONTRACT.md` + the `core/spi` interfaces + the seed data. |
| What is my risk? | Becoming the bottleneck. Fix: freeze the contracts in the first week, then work on your own screens. |

---

## Step 0 — Understand the project (2 hours)

Read, in this order:

1. `README.md` — the layout and the four team rules
2. `docs/INTEGRATION_CONTRACT.md` — how the four segments meet
3. `sql/01_schema.sql` — the data model (note the `OWNERSHIP OF THE TABLES` block)
4. `src/main/java/com/bubt/findit/core/model/Permission.java` — how access control works here

Do not write code yet. After these two hours you should be able to answer:
*"If Segment 4 approves a claim, which two tables change?"*
(Answer: `claims.status` → `APPROVED` and a new row in `audit_log`.)

---

## Step 1 — The shared data model (half a day)

**Files:** `core/model/*.java`

Build `User`, `Role`, `Permission`, `UserStatus`, `Session`, `OperationResult`.

**Why first:** every other segment types `user_id` into its own tables. If you
change `User` in week 3, three teammates have to change their code too.

**Rule:** these classes are *shared nouns*. Keep them small, keep them stable,
do not put business logic in them.

---

## Step 2 — Configuration and the database layer (half a day)

**Files:** `config/AppConfig.java`, `db/Database.java`, `db/dao/*`

- Read the connection settings from `app.properties`.
- Put every SQL statement behind an interface (`UserDao`), with a JDBC
  implementation and an in-memory implementation.
- `DaoFactory` picks the implementation.

**Why:** it lets the whole app (and the self-test) run without a MySQL server,
which means you never get stuck on "is it my code or the database?".

**Verify:** `java -cp target/classes com.bubt.findit.tools.HashTool test` prints
a hash — the `core/util` part works.

---

## Step 3 — The authentication service (1 day)

**Files:** `service/AuthService.java`, `service/SessionManager.java`, `core/util/PasswordHasher.java`

Build registration, login, logout, change password.

Rules that make this university-grade rather than average:

- **PBKDF2WithHmacSHA256** with a random 16-byte salt per user and 120,000
  iterations (`PasswordHasher`). Never MD5/SHA-1. Never plain text.
- **Constant-time comparison** when checking the hash.
- **The same error message** for "no such user" and "wrong password" — never
  reveal which one was wrong.
- **Sessions expire** and can be destroyed.
- **Log every attempt** to `audit_log`.

**Verify:** `java -cp target/classes:target/test-classes com.bubt.findit.SelfTest`

---

## Step 4 — Role-based access control (half a day)

**Files:** `service/RbacService.java`, `core/model/Permission.java`

One enum of permissions, one mapping to roles, one `require()` method.

**Why it is more than `if (role == ADMIN)`:** the moment a third role appears
("department admin", "moderator"), role checks scattered over 20 screens become
a bug farm. Permissions live in one file.

**Rule:** the check goes in the **service**, not in the button handler. The UI
may hide a button, but the service must refuse the action anyway.

---

## Step 5 — The integration layer (half a day) ← **the important one**

**Files:** `core/spi/*`, `core/spi/stub/*`, `core/event/*`, `core/registry/ServiceRegistry.java`, `core/AuthContext.java`

This is what makes Segment 1 the foundation instead of a wall.

Three mechanisms, and you need all three:

1. **Shared model** (`core.model`) — they use your `User`, you use nothing of theirs.
2. **Service contracts** (`core.spi`) — *you* declare the interface, *they*
   implement it, and the implementation arrives at run time through
   `ServiceRegistry`. You can therefore consume their data without compiling
   against their code.
3. **Events** (`core.event`) — they subscribe to your events; you never call
   them.

**The trick that makes this work:** ship **stub** implementations with the
contracts. The application always starts, even when Segments 2/3/4 have written
nothing yet. Their real implementation replaces one line:

```java
ServiceRegistry.register(ItemCatalogService.class, new ItemCatalogServiceImpl());
```

**Hand the contracts to the team at the end of this step.** Not earlier.

---

## Step 6 — The screens (1–2 days)

**Files:** `ui/common/UiTheme.java`, `ui/auth/LoginFrame.java`,
`ui/auth/RegisterFrame.java`, `ui/MainFrame.java`, `ui/dashboard/DashboardPanel.java`,
`ui/users/*`

Build in this order: theme → login → registration → main window → dashboard.

**The main window is the integration point for the UI.** `MainFrame` has a
sidebar whose entries are filtered by role, and every other segment's screen is
a panel inside the same window. Your teammates replace one placeholder — they
do not open a second window.

**Dashboard rule:** never query `items`, `matches` or `claims` yourself. Ask
`DashboardService`, which asks the other segments through their contracts.

---

## Step 7 — Make it provable (half a day)

**File:** `src/test/java/com/bubt/findit/SelfTest.java`

29 checks that run without a GUI and without a database. Keep them green —
they are the fastest way to prove that a change did not break anything.

Add one check per rule you add to the service layer.

---

## Step 8 — Document and hand over (half a day)

**Files:** `docs/INTEGRATION_CONTRACT.md`, `docs/SEGMENT1_DESIGN.md`, this file.

Give the team:
- the contracts (`core/spi`),
- the schema (`sql/01_schema.sql`),
- the seed data (`sql/02_seed_data.sql`),
- the demo credentials,
- the two rules: *own your tables* and *do not change the contracts without
  telling the team*.

---

## Your weekly routine

| When | What | Duration |
|---|---|---|
| Monday | Read the group chat, update the schema if a teammate needs a column | 15 min |
| Mid-week | Merge `main` into your branch, re-run `SelfTest` | 20 min |
| Friday | Post what you finished + what is blocked | 10 min |

**Never let a week pass without a merge.** Integration bugs are found in one
hour when the branches are one day old, and in one day when they are three
weeks old.

---

## Five mistakes that cost this project its marks last time

1. **Two segments writing the same table.** Decide the owner of every table in
   week 1 and write it in the schema file (done — see the header of
   `01_schema.sql`).
2. **Passing the user as a parameter through every method.** Use
   `AuthContext.currentUser()`. One source of truth.
3. **Checking permissions only in the UI.** A hidden button is not access
   control. Check in the service.
4. **Everyone writing their own copy of the login screen.** One login screen,
   built once, in `ui/auth`.
5. **Merging on the last night before submission.** Merge every week; the
   final week is for the report, not for the merge.

---

## How to demo your part in 3 minutes

1. Register a new student → show the validation (weak password, duplicate
   e-mail).
2. Log in with the **student ID** instead of the e-mail → show the flexibility.
3. Show the **dashboard**: your own counters, then the ones from Segment 2/3/4
   lighting up.
4. Log in as the admin → show **Manage Users** (suspend a student) → show the
   suspended student can no longer log in.
5. Show the **Audit Log** — every action of the demo is recorded.

That is 3 minutes, and it shows every part of your segment.
