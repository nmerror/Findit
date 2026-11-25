# Segment 1 Design — Authentication, RBAC & Dashboard

This document explains **how Segment 1 is built and why**. Read it once, then
change the code as you like — the important thing is that you keep the shape.

![FindIt architecture](images/architecture.png)

---

## 1. Layer responsibilities

| Layer | Package | May depend on | Never depends on |
|---|---|---|---|
| Presentation | `ui.*` | `service.*`, `core.*` | `db.*` |
| Service | `service.*` | `core.*`, `db.dao.*` | `ui.*`, Swing |
| Data access | `db.*` | `core.model` | `ui.*`, `service.*` |
| Shared | `core.*` | nothing inside the project | everything else |

The rule that makes the team fast: **dependencies point inwards only.**
A service never imports a Swing class, so it can be tested without a display
(see `SelfTest`). A DAO never knows about a screen.

---

## 2. Class inventory

### `core.model` — the shared nouns

| Class | Responsibility |
|---|---|
| `User` | The user record. The only place a student or admin exists. |
| `Role` | `STUDENT`, `ADMIN`. |
| `Permission` | Every capability in the system, plus the role → permission mapping. |
| `UserStatus` | `ACTIVE`, `PENDING`, `SUSPENDED`. |
| `Session` | A logged-in session: token, user, expiry. |
| `OperationResult<T>` | Success/failure + message + data, so services never throw at the screen. |
| `DashboardStats` | The counters the dashboard shows. |
| `ItemSummary`, `LocationCount`, `ItemType`, `ItemCategory` | Lightweight projections so Segment 1 does not need Segment 2's entity classes. |
| `Notification` | One in-app notification. |

### `core.spi` — the contracts (your team's API)

| Interface | Implemented by | Used by Segment 1 for |
|---|---|---|
| `ItemCatalogService` | Segment 2 | item counters, recent items, location chart |
| `ClaimQueryService` | Segment 3 | claim counters |
| `MatchQueryService` | Segment 3 | match counters |
| `AdminWorkloadService` | Segment 4 | admin workload counters |
| `NotificationService` | Segment 4 | "your account was suspended" alerts |
| `AuditService` | **Segment 1** | the audit trail — but the others may call it |

### `core.event` / `core.registry` / `core.AuthContext`

| Class | Responsibility |
|---|---|
| `EventBus` | Publish/subscribe. A failing listener can never break the publisher. |
| `AppEvent` | Event name + actor + payload map. |
| `Events` | The event names. Add yours here. |
| `ServiceRegistry` | The wiring hub: contract → implementation, swapped at start-up. |
| `AuthContext` | The session of the person using the app right now. |

### `ui` — the presentation layer

The GUI uses **one light theme and flat surfaces**. The standard Swing widgets
(table, scroll bar, tool tip) use the **Nimbus** look-and-feel with the
colours of `UiTheme`; the cards, buttons and charts are painted by hand. No
gradients, no glows, no custom title bar - the window keeps the decorations of
the operating system.

| Class | Responsibility |
|---|---|
| `UiTheme` | The design tokens: every colour, corner radius, spacing value and font size. |
| `Fonts` | Uses the system interface font, falls back to the Java logical font. |
| `BaseFrame` | Base class of every window: title, size, the panel for the content. |
| `RoundedPanel` | Card primitive: rounded corners, flat fill, hairline border. |
| `AppButton` | PRIMARY / SECONDARY / DANGER / SUBTLE buttons with a hover state. |
| `ModernTextField` / `ModernPasswordField` | Flat inputs with an accent focus border and a placeholder. |
| `NavButton` | Sidebar entry: vector glyph, label, hover tint, accent bar when active. |
| `TopBar` | Page header: title, live clock, search field, signed-in user. |
| `StatCard` | KPI card: caption, big number, supporting line, glyph badge. |
| `StackedBar` / `BarList` | Charts drawn with Graphics2D — no charting dependency. |
| `StatusBadge` / `StatusTableRenderer` | Coloured status pills, also inside table cells. |
| `ModernTable` | Light tables: soft stripes, styled header, fixed column widths. |
| `Icons` | 14 vector glyphs drawn in code — the project ships **no image files**. |
| `StretchPanel` | Makes a panel fill the width of its scroll pane (kills stray scrollbars). |
| `MainFrame` | Window shell: role-filtered sidebar + card layout + `addSegmentSection(...)`. |

**Rule for the other segments:** build your screens with these components.
Do not introduce new colours, new radii or new fonts — if something is missing,
add it to `UiTheme` and tell the team. That is what keeps four people
producing one product instead of four products.


| Class | Responsibility |
|---|---|
| `Database` | Owns the JDBC connection. |
| `DaoFactory` | Chooses the JDBC DAO or the in-memory DAO. |
| `UserDao` / `JdbcUserDao` / `InMemoryUserDao` | Data access for `users`. |
| `AuditDao` / `JdbcAuditDao` / `InMemoryAuditDao` | Data access for `audit_log`. |
| `AuthService` | Registration, login, logout, password change. |
| `SessionManager` | Creates, validates, destroys, expires sessions. |
| `RbacService` | `can()` / `require()` / `permissionsOf()`. |
| `UserService` | Profile, suspend, restore, change role. |
| `DashboardService` | Builds `DashboardStats` — **the only place that talks to the other segments**. |
| `AppServices` | The container that builds and hands out all services. |

---

## 3. The flows

### 3.1 Login

```
LoginFrame.performLogin()
  → AuthService.login(identifier, password)
      → UserDao.findByEmail(email)  or  findByStudentId(id)
      → PasswordHasher.verify(password, salt, hash)     ← constant time
      → if status == SUSPENDED  → refuse
      → SessionManager.create(user)  → Session(token, user, expiry)
      → UserDao.updateLastLogin(...)
      → AuthContext.set(session)                         ← the whole app now knows who it is
      → EventBus.publish(USER_LOGIN_SUCCESS)             ← others may react
      → AuditService.record("LOGIN", userId, "Signed in")
  → MainFrame opens
```

Note step 8: `AuthContext.set(session)` is what makes every later screen able
to ask "who is this?" with one line.

### 3.2 Dashboard refresh

```
DashboardPanel.refresh()
  → DashboardService.build(viewer)
      → UserDao counts                       (Segment 1 data)
      → ServiceRegistry.get(ItemCatalogService)   → counts, recent items, locations
      → ServiceRegistry.get(ClaimQueryService)    → claim counters
      → ServiceRegistry.get(MatchQueryService)    → match counters
      → ServiceRegistry.get(AdminWorkloadService) → admin workload
  → repaint the cards
```

Every call goes through the **interface**. If the implementation is still a
stub, the number is zero and the dashboard says
*"Waiting for: Segment 2 – Item Reporting"*. The moment the real
implementation is registered, the numbers appear — **with no change in
Segment 1's code**. That is the whole point.

### 3.3 Suspending a student

```
UserManagementPanel (admin only)
  → UserService.suspend(admin, userId)
      → rbac.require(admin, Permission.MANAGE_USERS)   ← refused if not allowed
      → refuse to suspend yourself
      → UserDao.updateStatus(userId, SUSPENDED)
      → EventBus.publish(USER_STATUS_CHANGED)
      → NotificationService.notifyUser(userId, "Account suspended", ...)
      → AuditService.record("SUSPEND_USER", adminId, ...)
```

The next login attempt by that student is refused by `AuthService` with
`ACCOUNT_SUSPENDED`.

---

## 4. Security decisions (and why)

| Decision | Why |
|---|---|
| PBKDF2WithHmacSHA256, 120,000 iterations, 16-byte random salt | Standard, in the JDK, and slow enough to make offline cracking expensive. MD5/SHA-1/plain text would cost you marks. |
| `MessageDigest.isEqual` for comparing hashes | Constant time — the comparison does not leak how much matched. |
| The same error for "unknown user" and "wrong password" | Otherwise an attacker can discover which e-mails are registered. |
| The admin account is seeded, never self-registered | A registration form that can create admins is a security hole. |
| Permission checks in the **service**, not the button | A hidden button is not access control — the service must refuse too. |
| Every login attempt is audited | Failed logins are how you spot a brute-force attempt. |
| Password never leaves the service layer | The UI receives `User.sanitized()`, which has no hash and no salt. |

---

## 5. Extension points — "how do I add X?"

**A new permission**

1. Add it to `core/model/Permission.java` with its role mapping.
2. Use it: `rbac.require(user, Permission.NEW_THING)`.
3. It appears automatically in `rbac.permissionsOf(user)`.

**A new screen for another segment**

1. Write the panel in their own package.
2. One line in `MainFrame.buildContent()`: `content.add(new TheirPanel(...), "card-name");`
3. One line in `MainFrame.buildNavigation()`: `addNavButton("Their Screen", "card-name");`

**A new table**

1. Add it to `sql/01_schema.sql` with the owning segment in the header comment.
2. Add a DAO interface + a JDBC implementation + an in-memory implementation.
3. Register it in `DaoFactory`.

**A new event**

1. Add the name to `core/event/Events.java`.
2. Publish it: `eventBus.publish(AppEvent.of(Events.NEW_EVENT).with("k", v).build());`
3. Subscribe on the other side.

---

## 6. Trade-offs you can explain in the viva

- **Sessions in memory, not in the database.** A desktop application dies with
  the process, so the session dies with it. A server application would need a
  session table — the `SessionManager` interface is small enough to swap.
- **No dependency-injection framework.** `AppServices` does the job in 40
  lines. Adding Spring would be impressive but unreadable for most of the team.
- **Plain JDBC, not JPA/Hibernate.** Section 9 of the proposal asks for MySQL
  and JDBC; showing real SQL is worth more than hiding it.
- **No password reset by e-mail.** A real product needs a mail server. The
  admin can restore a suspended account, and the seed data gives you working
  credentials for the demo.

---

## 7. What is left for you to build

Everything in this repository is a **working reference** — read it, then make
it yours. The parts that are deliberately thin, and that you should extend:

- [ ] **Password reset flow** (admin-triggered, sets a temporary password)
- [ ] **Profile photo upload** (reuses the `item_images` table with
      `image_type = 'AVATAR'`)
- [ ] **Session timeout handling in the UI** — a Swing timer that calls
      `sessionManager.purgeExpired()` and logs the user out
- [ ] **Brute-force protection** — count failed logins per user in
      `audit_log`, lock the account after 5
- [ ] **Dashboard charts** instead of the plain counters (the data is already
      there — draw it with `JFreeChart` or plain `Graphics2D`)
- [ ] **Unit tests with JUnit** — `SelfTest` shows the structure; convert it
      when you are comfortable
