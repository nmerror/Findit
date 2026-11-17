# FindIt — Segment 1 Skeleton

**FindIt – Campus Lost & Found Management System**
Segment 1: *User Authentication, Role-Based Access Control & Dashboard Management*

This is the foundation of the whole project. It contains the identity model,
the RBAC rules, the login/registration screens, the dashboard, and — most
importantly — the **contracts** that let Segments 2, 3 and 4 plug in without
Segment 1 ever changing.

---

## 1. Run it right now (no database needed)

```bash
cd findit
java -cp target/classes com.bubt.findit.Main --memory
```

Add `--demo` and the dashboard fills up with **fake data from Segments 2/3/4** —
so you can present a complete dashboard before your teammates have written
anything:

```bash
java -cp target/classes com.bubt.findit.Main --memory --demo
```

or with Maven:

```bash
mvn -q clean package
java -cp "target/classes:target/lib/*" com.bubt.findit.Main --memory
```

| Flag | Effect |
|---|---|
| `--memory` | runs everything in RAM, no database server needed |
| `--demo` | installs fake Segment 2/3/4 data so the dashboard looks complete |

---

## 2. The screens

| Screen | File | What it shows |
|---|---|---|
| Login / Registration | `ui/auth/LoginFrame.java` | branded hero + glass sign-in card, focus rings, validation |
| Dashboard / Main Menu | `ui/dashboard/DashboardPanel.java` | 4 KPI cards, claim-pipeline donut, location bars, live table |
| Data Management | `ui/users/UserManagementPanel.java` | admin-only: suspend / restore / change role |
| Search / Report | `ui/search/SearchResultsPanel.java`, `ui/reports/ReportPanel.java` | global search + text/CSV export |
| Additional | `ui/users/AuditLogPanel.java`, `ui/users/ProfilePanel.java` | audit trail, profile + activity feed |

> PNG screenshots of every screen live in `docs/images/`. They are not
> committed here because the GitHub API used for this upload is text-only.
> Regenerate any of them with:
> `java -cp target/classes com.bubt.findit.tools.Screenshot dashboard out.png`
> (prefix the screen with `light:` for the light palette).

---

## 3. Two modes: dark and light

The sun / moon button in the window title bar switches the whole application
between the **dark** palette and the **light** palette — instantly, without a
restart. The chosen mode is saved in `app.properties` (`app.theme = dark`).

Both palettes live in `ui/common/UiTheme.java`; the screen is rebuilt around
the new palette, so nothing is left in the old colours.

---

## 4. The interface — a small design system

The GUI is **100% custom-painted Swing**: no default look-and-feel widgets are
visible anywhere, so the project looks the same on every machine.

| Component | What it gives you |
|---|---|
| `UiTheme` | Every colour, radius and font in one file (deep-space navy + cyan/violet accents) |
| `Fonts` | Picks the best installed modern typeface, falls back safely |
| `BaseFrame` | Undecorated window with a custom title bar, app mark and window buttons |
| `RoundedPanel` | Cards with rounded corners, gradient fill, hairline border and soft glow |
| `GlowButton` | Primary / ghost / danger buttons with gradient and hover glow |
| `ModernTextField` | Rounded dark input with a luminous focus ring and placeholder |
| `NavButton` | Sidebar item with vector glyph and an accent bar when active |
| `StatCard` | KPI card — caption, big number, glyph badge |
| `DonutChart` / `BarList` | Charts drawn with Graphics2D (no charting library) |
| `StatusBadge` / `StatusTableRenderer` | Coloured status pills, also inside tables |
| `Icons` | 14 vector glyphs drawn in code — the project ships **no image files** |
| `ModernTable` | Dark tables: no grid lines, striped rows, styled header |
| `GradientBackdrop` | The glowing gradient behind the login screen |
| `StretchPanel` | Makes content fill the width of a scroll pane (no stray scrollbars) |

Your teammates should build their screens with **these** components instead of
inventing their own colours — that is how the four segments end up looking
like one product.

---

## 5. Run it with MySQL (what the proposal asks for)

```bash
mysql -u root -p < sql/01_schema.sql
mysql -u root -p < sql/02_seed_data.sql
```

Edit `src/main/resources/app.properties` (or put an `app.properties` next to
your jar) with your MySQL user and password, then:

```bash
java -cp "target/classes:target/lib/*" com.bubt.findit.Main
```

Seeded logins:

| Account | E-mail | Password | Role |
|---|---|---|---|
| Administrator | `admin@findit.edu` | `Admin@1234` | ADMIN |
| Student | `ayesha@buet.edu` | `Student@123` | STUDENT |

## 6. Verify the Segment 1 logic (no GUI, no database)

```bash
java -cp target/classes:target/test-classes com.bubt.findit.SelfTest
```

**43 checks**: registration validation, login (e-mail *and* student ID),
suspension, RBAC, session expiry, the event bus, the global search, the report
builder, and — the important one — the dashboard and the search picking up a
fake Segment 2 implementation **through the contract, with no change in
Segment 1's code**.

---

## 7. Project layout

![architecture](docs/images/architecture.svg)

```
findit/
├── pom.xml
├── sql/
│   ├── 01_schema.sql          ← database contract for all 4 segments
│   └── 02_seed_data.sql
├── docs/
│   ├── START_HERE.md           ← your day-by-day plan
│   ├── SEGMENT1_DESIGN.md      ← how Segment 1 is designed
│   ├── INTEGRATION_CONTRACT.md ← what your teammates must implement
│   └── images/
└── src/main/java/com/bubt/findit/
    ├── core/                   ← SHARED CODE — everybody may use it
    │   ├── model/              ← User, Role, Permission, Session, DashboardStats…
    │   ├── spi/                ← SERVICE CONTRACTS (implemented by Segments 2/3/4)
    │   │   └── stub/           ← temporary placeholders so the app always runs
    │   ├── event/              ← EventBus, AppEvent, Events
    │   ├── registry/           ← ServiceRegistry (the wiring hub)
    │   ├── util/               ← PasswordHasher, Validation
    │   └── AuthContext.java    ← "who is using the app right now"
    ├── db/                     ← JDBC layer (Segment 1 owns users + audit_log)
    │   └── dao/
    ├── service/                ← business rules (Segment 1)
    ├── ui/                     ← Swing screens (Segment 1)
    ├── app/AppServices.java    ← dependency container
    ├── config/AppConfig.java
    └── Main.java
```

---

## 8. The four rules that keep the team out of merge conflicts

1. **A segment only writes its own tables.** Segment 1 owns `users` and
   `audit_log`; Segment 2 owns `locations`, `items`, `item_images`; Segment 3
   owns `matches`, `claims`; Segment 4 writes only `claims` and `audit_log`.
   Everything else is read through a foreign key.

2. **Segment 1 never imports a class from Segment 2, 3 or 4.** It only uses the
   interfaces in `core.spi`. Their implementations arrive at run time through
   `ServiceRegistry`. This is what makes the merge painless.

3. **Nobody calls `AuthContext.currentUser()` → then asks "who is this?"
   again.** `AuthContext` is the single source of the current user.

4. **Permission checks live in the service layer, not in the button handler.**
   `rbac.require(user, Permission.APPROVE_CLAIM)` — the UI is not a security
   boundary.

---

## 9. Where to start (full plan in `docs/START_HERE.md`)

| Step | What you build | Why it is first |
|---|---|---|
| 1 | `sql/01_schema.sql` + `core/model` | Everyone codes against the same data model |
| 2 | `db/dao` + `config/AppConfig` | Data access behind an interface |
| 3 | `service/AuthService` + `SessionManager` | The core of Segment 1 |
| 4 | `service/RbacService` + `Permission` | Decides what each role may do |
| 5 | `core/spi` + `core/event` + `ServiceRegistry` | **The integration layer** |
| 6 | `ui/auth` + `ui/MainFrame` + `ui/dashboard` | The screens |
| 7 | `docs/INTEGRATION_CONTRACT.md` + team meeting | Hand the contracts to the team |

> **Do step 5 early.** The moment your teammates can code against your
> interfaces, you stop being the bottleneck for the rest of the project.
