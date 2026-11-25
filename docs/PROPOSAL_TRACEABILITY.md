# Proposal Traceability — what belongs to Segment 1

**Project:** FindIt: Campus Lost and Found System
**Course:** Advanced Programming Lab (CSE 342) · BUBT · Intake 55-4
**Team member:** Sadikul Basar (20255103159) — Co-Leader
**My segment:** Segment 1 — *User Authentication, Role-Based Access Control &
Dashboard Management*

This file answers one question: **for every section of the official proposal,
where is my code?** Use it when you fill the proposal and when the teacher
asks "who did what?".

---

## 1. Project Information (table 1)

| Field in the proposal | Value | My code |
|---|---|---|
| Project title | FindIt: Campus Lost and Found System | — |
| Application type | Desktop Application (GUI-based) | `ui/*` — Swing, no web server |
| Programming language | Java | `src/main/java/com/bubt/findit/` |
| GUI framework | Java Swing | every screen in `ui/` is Swing |
| DBMS | **SQLite** → **change this cell to MySQL** | `db/` uses plain JDBC |
| Connectivity | JDBC | `db/Database.java`, `db/dao/Jdbc*.java` |
| IDE | IntelliJ IDEA + VS Code | `pom.xml` (Maven) works in both |

> **Correction to make in the proposal:** the table says *SQLlite*, but
> Section 9 of the same proposal states *"The project must use MySQL and JDBC
> for persistent data storage"*. Change the cell to **MySQL 8** so the two
> parts of the proposal agree.

---

## 2. Section 6 — Proposed Features and Functionalities

| Row in the proposal | Owner | Where it lives |
|---|---|---|
| **User Login and Authentication** | **Segment 1 (me)** | `service/AuthService.java`, `ui/auth/LoginFrame.java`, `ui/auth/RegisterFrame.java` |
| Module 1 | Segment 2 | `MainFrame.addSegmentSection(...)` — the slot is created on demand |
| Module 2 | Segment 3 | same |
| Module 3 | Segment 4 | same |
| **Search and Report Generation** | **Segment 1 (me)** + Segment 2 (item filters) | `service/SearchService.java`, `service/ReportService.java`, `ui/search/`, `ui/reports/` |

---

## 3. Section 7 — GUI Design and Screens

| Required screen | Owner | My class |
|---|---|---|
| 1. Login / Registration Screen | **Segment 1 (me)** | `ui/auth/LoginFrame.java`, `ui/auth/RegisterFrame.java` |
| 2. Dashboard / Main Menu | **Segment 1 (me)** | `ui/MainFrame.java`, `ui/dashboard/DashboardPanel.java` |
| 3. Data Management Screen | **Segment 1 (me)** | `ui/users/UserManagementPanel.java` (admin), `ui/users/ProfilePanel.java` |
| 4. Search / Report Screen | **Segment 1 (me)** | `ui/search/SearchResultsPanel.java`, `ui/reports/ReportPanel.java` |
| Additional screen | **Segment 1 (me)** | `ui/users/AuditLogPanel.java` (audit trail) |

**Segment 1 therefore delivers 4 of the 4 required screens + 1 additional
screen.** The other segments add their screens inside the same window with
one call — `MainFrame.addSegmentSection(section, item, cardName, panel)` puts
the screen in the navigation, registers it and keeps track of its title.

---

## 4. Section 8 — Application of Java OOP Concepts

Every row of the table is demonstrated in Segment 1 — this is the part of the
proposal I can show in the viva without any help from the team.

| OOP concept | Where it is in my code |
|---|---|
| Classes and Objects | `core/model/User.java`, `Role.java`, `Session.java`, `OperationResult.java` |
| Encapsulation | private fields + getters/setters in `User`, `Session`; `User.sanitized()` hides the password hash |
| **Inheritance** | `ui/common/BaseFrame.java` is an **abstract class**; `LoginFrame`, `RegisterFrame` and `MainFrame` **extend** it |
| **Polymorphism** | *overriding*: every DAO interface has two implementations that override the same methods — `db/dao/UserDao.java` → `JdbcUserDao` (MySQL) and `InMemoryUserDao` (demo mode), the same for `AuditDao` and `NotificationDao`; *overloading*: `PasswordHasher.hash(...)` and `ServiceRegistry.register(...)` |
| Abstraction | the six **interfaces** in `core/spi/`, the **abstract class** `BaseFrame`, the DAO interfaces in `db/dao/` |
| Exception Handling | `try/catch` in the DAOs, custom `AccessDeniedException` and `DataAccessException`, `OperationResult` instead of exceptions across the UI |
| Collections | `ArrayList`, `HashMap`, `ConcurrentHashMap`, `CopyOnWriteArrayList`, `List.of()` in `SearchService`, `ServiceRegistry`, `EventBus` |
| Event Handling | Swing listeners (`ActionListener`, `MouseAdapter`) **and** the publish/subscribe `core/event/EventBus.java` |

---

## 5. Section 9 — Database Design and JDBC Connectivity

### Tables I own

| Table | Purpose | Primary key | Owner |
|---|---|---|---|
| `users` | accounts, roles, password hashes | `user_id` | **Segment 1 (me)** |
| `audit_log` | every important action | `audit_id` | **Segment 1 (me)** |
| `notifications` | in-app alerts | `notification_id` | **Segment 1 (me)** |

The other tables (`locations`, `items`, `item_images`, `matches`, `claims`)
belong to Segments 2, 3 and 4 — see the header of `sql/01_schema.sql`.

### The planned database operations (the tick boxes)

| Operation | Where |
|---|---|
| INSERT | `JdbcUserDao.insert()` (register), `JdbcAuditDao.insert()` (audit) |
| SELECT | `JdbcUserDao.findByEmail/findById/findAll()`, `findOne` helper |
| UPDATE | `JdbcUserDao.updatePassword/updateStatus/updateRole/updateLastLogin()` |
| **DELETE** | `JdbcAuditDao.deleteOlderThan()` — "clear entries older than 30 days" button on the Audit Log screen |
| SEARCH / FILTER | `service/SearchService.java` (global search) and the SQL `WHERE` clauses in the DAOs |
| JOIN / REPORT | `service/ReportService.java` — the report joins the users, the audit trail and the counters the other segments publish |

### JDBC (PreparedStatement)

Every statement in `db/dao/JdbcUserDao.java` and `db/dao/JdbcAuditDao.java`
uses `PreparedStatement` with `?` placeholders — no string concatenation of
values, so there is no SQL-injection hole. The connection comes from
`db/Database.java`, which loads the MySQL driver by name and keeps the
credentials in `app.properties`.

---

## 6. Section 10 — Tools and Technologies

| Tool | Version I used | Where |
|---|---|---|
| JDK | 17 | `pom.xml` (`maven.compiler.source`) |
| Java Swing | part of the JDK | all of `ui/` |
| MySQL | 8.x | `sql/01_schema.sql` |
| JDBC driver | `mysql-connector-j` 8.4.0 | `pom.xml` |
| IDE | IntelliJ IDEA / VS Code | both work — the project is a plain Maven project |
| Extra | Maven 3.9, Git | `pom.xml`, `.gitignore` |

---

## 7. Section 12 — Project Timeline (my part)

| Phase | Target | Segment 1 deliverable |
|---|---|---|
| Requirement analysis | week 1 | this traceability file + the feature list |
| GUI and system design | week 1-2 | ER schema (`sql/01_schema.sql`), UML-style class index (`docs/SEGMENT1_FILE_INDEX.md`) |
| Database creation and JDBC | week 2 | `users`, `audit_log`, `notifications` + `db/` layer |
| OOP implementation and GUI | week 3-4 | auth, RBAC, dashboard, login/register screens |
| Integration | week 5 | contracts (`core/spi`) + service registry so the team can plug in |
| Testing and documentation | week 6 | `SelfTest` (43 checks) + the three team documents |

---

## 8. Section 14 — Team Member Contribution

Copy this into your row of the table:

> **Sadikul Basar (20255103159)** — Segment 1: User Authentication, Role-Based
> Access Control & Dashboard Management. Designed the shared data model
> (`User`, `Role`, `Permission`) and the MySQL schema; implemented
> registration and login with PBKDF2 password hashing and salted hashes,
> session management with expiry, a permission-based access control service
> and the audit trail; built the login, registration, dashboard, profile,
> user-management, audit-log, global-search and report screens in Java Swing
> with a flat, light design system; and published the
> service contracts and event bus that the other segments implement to plug
> into the application.

---

## 9. What I give the other segments (the "connect" part)

| They get | How they use it |
|---|---|
| `core/spi/ItemCatalogService` | implement it → the dashboard shows item counts |
| `core/spi/ClaimQueryService` | implement it → the dashboard shows claim counts |
| `core/spi/MatchQueryService` | implement it → the dashboard shows match counts |
| `core/spi/AdminWorkloadService` | implement it → the admin dashboard shows the workload |
| `core/spi/NotificationService` | implement it → "your claim was approved" reaches the user |
| `core/spi/SearchContributor` *(optional)* | implement it → their rows appear in the global search |
| `core/event/EventBus` | subscribe → react to `USER_REGISTERED`, `USER_LOGIN_SUCCESS`, … |
| `AuthContext.currentUser()` | read → who is signed in |
| `RbacService.require(user, Permission.X)` | call → guard their actions |
| `AuditService.record(...)` | call → their actions appear in the audit log |
| `users.user_id` | reference → the real link between all four segments |

Full detail: `docs/INTEGRATION_CONTRACT.md`.
