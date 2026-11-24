# Segment 1 File Index — what every file does

**90 Java files.** Read this page and you know the whole project without
opening a single class.

> **How to read a file name**
> `core/spi/ItemCatalogService.java` = *package* `core.spi` (the contracts)
> → *class* `ItemCatalogService` (the thing other segments implement).
> The package tells you who may use it:
> `core` = everybody · `db`/`service`/`ui` = Segment 1 only ·
> `tools` = helper programs.

---

## The five packages

| Package | Who may use it | What lives in it |
|---|---|---|
| `core` | **everybody** | shared nouns, the contracts, the event bus |
| `db` | Segment 1 | JDBC access to `users` and `audit_log` |
| `service` | Segment 1 | business rules (no Swing code) |
| `ui` | Segment 1 | the Swing screens |
| `app`, `config`, `tools` | Segment 1 | wiring, settings, helper programs |

---

## 1. `core/` — the shared foundation (32 files)

### `core/model/` — the shared nouns (13 files)
| File | One-line purpose |
|---|---|
| `User.java` | The user record: name, e-mail, student ID, role, status, password hash |
| `Role.java` | `STUDENT` and `ADMIN` |
| `Permission.java` | Every capability in the system + the role → permission mapping |
| `UserStatus.java` | `ACTIVE`, `PENDING`, `SUSPENDED` |
| `Session.java` | A logged-in session: token, user, expiry |
| `OperationResult.java` | Success/failure + message + data (no exceptions in the UI) |
| `DashboardStats.java` | All the counters the dashboard shows |
| `SearchHit.java` | One row of the global search result |
| `ItemSummary.java` | Lightweight item view (Segment 2's data without their classes) |
| `LocationCount.java` | "Library — 12 items" |
| `ItemType.java` | `LOST` / `FOUND` |
| `ItemCategory.java` | Electronics, Documents, Accessories, … |
| `Notification.java` | One in-app notification |

### `core/spi/` — **the contracts your teammates implement** (7 files)
| File | Implemented by | What it gives Segment 1 |
|---|---|---|
| `ItemCatalogService.java` | **Segment 2** | item counts, recent items, location chart |
| `ClaimQueryService.java` | **Segment 3** | claim counters |
| `MatchQueryService.java` | **Segment 3** | match counters |
| `AdminWorkloadService.java` | **Segment 4** | admin workload counters |
| `NotificationService.java` | **Segment 4** | "your claim was approved" alerts |
| `AuditService.java` | Segment 1 | the audit trail (the others may call it) |
| `SearchContributor.java` | *anyone, optional* | joins the global search |

### `core/spi/stub/` — placeholders (5 files)
`StubItemCatalogService` · `StubClaimQueryService` · `StubMatchQueryService` ·
`StubAdminWorkloadService` · `StubNotificationService`
→ They let the application run before the other segments exist. Each one is
replaced by one line at start-up.

### `core/event/` — publish / subscribe (4 files)
| File | Purpose |
|---|---|
| `EventBus.java` | The bus: `subscribe()` / `publish()`; a failing listener never breaks the publisher |
| `AppEvent.java` | The event: name + actor + payload |
| `EventListener.java` | The callback interface |
| `Events.java` | The event names (`USER_REGISTERED`, `USER_LOGIN_SUCCESS`, …) |

### The rest of `core/` (4 files)
| File | Purpose |
|---|---|
| `AuthContext.java` | "Who is using the app right now?" — one line for everybody |
| `registry/ServiceRegistry.java` | The wiring hub: contract → implementation, swapped at start-up |
| `util/PasswordHasher.java` | PBKDF2WithHmacSHA256, per-user salt, constant-time compare |
| `util/Validation.java` | E-mail, student ID and password rules |

---

## 2. `db/` — the database layer (10 files)

| File | Purpose |
|---|---|
| `Database.java` | Owns the pool, hands out connections, runs transactions |
| `ConnectionPool.java` | The pool: a connection is *returned* instead of closed |
| `DaoFactory.java` | Chooses the JDBC DAO or the in-memory DAO |
| `dao/UserDao.java` | Interface: read/insert/update/delete the `users` table |
| `dao/JdbcUserDao.java` | The MySQL implementation (PreparedStatement everywhere) |
| `dao/InMemoryUserDao.java` | The in-memory implementation (no server needed) |
| `dao/AuditDao.java` | Interface: read/insert/delete the `audit_log` table |
| `dao/JdbcAuditDao.java` | The MySQL implementation |
| `dao/InMemoryAuditDao.java` | The in-memory implementation |
| `dao/AuditEntry.java` | One row of the audit log |
| `dao/NotificationDao.java` | Interface: the in-app notifications |
| `dao/JdbcNotificationDao.java` + `InMemoryNotificationDao.java` | The two implementations |
| `dao/ItemDao.java` + `dao/JdbcItemDao.java` | Segment 2's items and locations |
| `dao/ClaimDao.java` + `dao/JdbcClaimDao.java` | Segment 3's claims |
| `dao/MatchDao.java` + `dao/JdbcMatchDao.java` | Segment 3's matches |
| `dao/ItemRecord.java`, `ClaimRecord.java`, `MatchRecord.java` | The write-side rows |
| `dao/DataAccessException.java` | Wraps `SQLException` so the service layer stays clean |

---

## 3. `service/` — the business rules (9 files)

| File | Purpose |
|---|---|
| `AuthService.java` | Register, log in, log out, change password |
| `SessionManager.java` | Creates, validates, expires and destroys sessions |
| `RbacService.java` | `can()` / `require()` / `permissionsOf()` — the permission gate |
| `UserService.java` | Profile, suspend, restore, change role, clear old audit entries |
| `DashboardService.java` | Builds the dashboard counters — **the only place that talks to the other segments** |
| `SearchService.java` | The global search (users + every registered contributor) |
| `ReportService.java` | Builds the text/CSV report and exports it |
| `AuditServiceImpl.java` | Writes into `audit_log` |
| `AccessDeniedException.java` | Thrown when a role is not allowed to do something |

### `service/data/` — the database-backed contracts (5 files)
| File | Purpose |
|---|---|
| `ItemCatalogServiceImpl.java` | Segment 2's contract, reading the real `items` table |
| `ClaimQueryServiceImpl.java` | Segment 3's claim contract, reading the real `claims` table |
| `MatchQueryServiceImpl.java` | Segment 3's match contract, reading the real `matches` table |
| `AdminWorkloadServiceImpl.java` | Segment 4's workload contract, reading the real tables |
| `NotificationServiceImpl.java` | Segment 1's notifications, stored in the database |

They are **reference implementations** — the owning segment can replace any
of them with their own class and nothing else has to change.

---

## 4. `ui/` — the screens (31 files)

### `ui/common/` — the design system (22 files) — **your teammates should use these**
| File | Purpose |
|---|---|
| `UiTheme.java` | Every colour, radius and spacing value — plus the **dark/light palettes** |
| `Fonts.java` | Picks the best installed modern typeface, falls back safely |
| `BaseFrame.java` | **Abstract** base class of every window: custom title bar, theme switch, drag |
| `ThemeToggle.java` | The sun / moon button that switches the two modes |
| `RoundedPanel.java` | Card primitive: rounded corners, gradient, hairline border, glow |
| `GlowButton.java` | Primary / ghost / danger buttons with a hover glow |
| `ModernTextField.java` | Rounded input with a luminous focus ring and placeholder |
| `ModernPasswordField.java` | The password twin |
| `FieldSkin.java` | The painting code shared by the two input fields |
| `NavButton.java` | Sidebar item with a vector glyph and an active accent bar |
| `TopBar.java` | Page header: title, live clock, **global search box**, user |
| `StatCard.java` | KPI card: caption, big number, glyph badge |
| `DonutChart.java` | The claim-pipeline ring, drawn with Graphics2D |
| `BarList.java` | The "busiest locations" bars |
| `StatusBadge.java` | A coloured status pill |
| `StatusTableRenderer.java` | Puts the pill inside a table cell |
| `ModernTable.java` | Dark tables: no grid lines, striped rows, styled header |
| `Icons.java` | 16 vector glyphs drawn in code — the project ships no images |
| `GradientBackdrop.java` | The glowing gradient behind the login screen |
| `StretchPanel.java` | Makes a panel fill the width of a scroll pane |
| `PlaceholderPanel.java` | The styled slot where the other segments put their screens |

### The screens (9 files)
| File | Screen |
|---|---|
| `ui/MainFrame.java` | The window: role-based sidebar + header + all the screens |
| `ui/auth/LoginFrame.java` | Screen 1 — login (hero + glass card) |
| `ui/auth/RegisterFrame.java` | Screen 1 — registration |
| `ui/dashboard/DashboardPanel.java` | Screen 2 — KPI cards, donut, bars, live table |
| `ui/users/UserManagementPanel.java` | Screen 3 — data management (admin only) |
| `ui/users/ProfilePanel.java` | My profile + password change + activity feed |
| `ui/users/AuditLogPanel.java` | Additional screen — the audit trail |
| `ui/search/SearchResultsPanel.java` | Screen 4 — global search results |
| `ui/reports/ReportPanel.java` | Screen 4 — report generation and export |

---

## 5. The rest (8 files)

| File | Purpose |
|---|---|
| `Main.java` | Entry point: reads the config, starts the app (`--memory`, `--demo`) |
| `app/AppServices.java` | Builds every service once and hands them to the screens |
| `config/AppConfig.java` | Reads `app.properties` (database, theme, session timeout) |
| `tools/HashTool.java` | Prints a password hash + salt (for seeding the database) |
| `tools/Screenshot.java` | Takes a screenshot of any screen — for the report and the slides |
| `tools/DemoSegmentData.java` | Fake Segment 2/3/4 data so the dashboard can be presented early |
| `src/test/java/.../SelfTest.java` | 43 checks that run without a GUI and without a database |
| `src/test/java/.../BackendSelfTest.java` | 49 checks against the real MySQL database |
| `sql/01_schema.sql` | The database schema — the contract between the segments |

---

## 6. The three documents

| File | Read it when |
|---|---|
| `docs/PROPOSAL_TRACEABILITY.md` | you fill the proposal or you answer "who did what?" |
| `docs/BACKEND_DESIGN.md` | you want to change the database layer or add a table |
| `docs/INTEGRATION_CONTRACT.md` | you explain to a teammate what they must implement |
| `docs/SEGMENT1_DESIGN.md` | you want to know why the code is built this way |
| `docs/START_HERE.md` | you want the day-by-day build order |
