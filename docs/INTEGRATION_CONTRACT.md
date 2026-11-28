# Integration Contract — How FindIt's Four Segments Fit Together

**Owner:** Segment 1 (Authentication, RBAC & Dashboard)
**Audience:** Segment 2, Segment 3, Segment 4
**Status:** freeze this document before the others start coding. If it changes,
say so in the team group chat on the same day.

---

## 1. The one-sentence version

> Segment 1 gives everybody the **user** and the **permission system**, and
> defines **the interfaces** that the other segments implement — so nobody has
> to touch anybody else's code.

---

## 2. What Segment 1 gives you (use it, do not rebuild it)

| You need | Use this | Never do this |
|---|---|---|
| "Who is the current user?" | `AuthContext.currentUser()` | Passing the user around in every method |
| "Who is allowed to do this?" | `RbacService.require(user, Permission.X)` | `if (user.getRole() == Role.ADMIN)` |
| "Is this user an admin?" | `user.isAdmin()` | Comparing role strings |
| "Write a log entry" | `AuditService.record(action, actorId, details)` | Writing your own log table |
| "Tell the user something" | `NotificationService.notifyUser(id, title, text)` | Printing to the console |
| "Show a nice screen" | `UiTheme` colours, fonts, `RoundedPanel`, `AppButton`, `ModernTable` | Inventing new colours per screen |
| "Put a panel in the navigation" | `mainFrame.addSegmentSection(section, item, cardName, panel)` | Opening a new JFrame from your code |

### How to add your screen to the window

Segment 1 does **not** show empty placeholder screens — the navigation only
lists the screens that really exist. When your screen is ready, add it with
one call:

```java
mainFrame.addSegmentSection(
        "Segment 2 - Reporting",   // the heading of the group in the sidebar
        "Report an Item",          // the label of the entry
        "s2-report",               // an internal name, use it for navigation
        new ReportItemPanel(services));
```

The window creates the entry in the sidebar, registers the panel, keeps track
of the title of the screen and refreshes the header. Nothing else is needed.

---

## 3. What YOU have to implement (Segment 2)

**Contract:** `core/spi/ItemCatalogService.java`

```java
List<ItemSummary> recentItems(int limit);      // newest first, never null
int  countByType(ItemType type);               // LOST / FOUND
int  countByCategory(ItemCategory category);
List<LocationCount> topLocations(int limit);    // "Library - 12"
```

**Your tables:** `locations`, `items`, `item_images`
**Your screens:** "Report Lost / Found Item", "Browse Items"

**Register it** (one line, in your own bootstrap — never inside Segment 1):

```java
ServiceRegistry.register(ItemCatalogService.class, new ItemCatalogServiceImpl());
```

**Checklist**

- [ ] `items.reported_by` → `users.user_id` (foreign key, never a free-text name)
- [ ] Your service returns empty lists / zeros instead of `null`
- [ ] `isAvailable()` returns `true` in the real implementation
- [ ] Your screen is added with `mainFrame.addSegmentSection(...)` — nothing else
- [ ] You call `rbac.require(user, Permission.REPORT_ITEM)` before saving an item

---

## 4. What YOU have to implement (Segment 3)

**Contracts:** `core/spi/ClaimQueryService.java`, `core/spi/MatchQueryService.java`

```java
int countOpenClaims();
int countPendingVerification();
int countApprovedClaims();
int countReturnedItems();
int countClaimsByUser(long userId);

int countNewMatches();
int countConfirmedMatches();
int countIndexedItems();
```

**Your tables:** `matches`, `claims`
**Your screens:** "My Claims", "Matching Results"

**Register them:**

```java
ServiceRegistry.register(ClaimQueryService.class, new ClaimQueryServiceImpl());
ServiceRegistry.register(MatchQueryService.class, new MatchQueryServiceImpl());
```

**Checklist**

- [ ] `claims.claimant_id` → `users.user_id`, `claims.verified_by` → `users.user_id`
- [ ] The claimant is always `AuthContext.currentUser()` — never taken from a form field
- [ ] The matching engine writes into `matches` only, never into `items`
- [ ] Publishing an event when a new match is found (see §7)

---

## 5. What YOU have to implement (Segment 4)

**Contract:** `core/spi/AdminWorkloadService.java`

```java
int countReportsPendingReview();
int countClaimsPendingVerification();
int countAwaitingReturnHandover();
```

**Your tables:** you may update `claims` (status) and insert into `audit_log`.
You must not touch `users` except through `UserService` (suspend / restore).

**Your screens:** "Verify Claims", "Return Management" — add them with
`mainFrame.addSegmentSection(...)`; both are visible to admins only.

**Register it:**

```java
ServiceRegistry.register(AdminWorkloadService.class, new AdminWorkloadServiceImpl());
```

**Checklist**

- [ ] Every decision is written to the audit log:
      `auditService.record("CLAIM_APPROVED", adminId, "claim #12 approved")`
- [ ] Approving a claim also calls `notificationService.notifyUser(claimantId, ...)`
- [ ] Every admin action is guarded by `rbac.require(user, Permission.APPROVE_CLAIM)`

---

## 6. The shared `users` table — the real connection point

Every segment is joined to every other segment through one column:

```
users.user_id
   ├── items.reported_by        (Segment 2)
   ├── item_images.uploaded_by  (Segment 2)
   ├── claims.claimant_id       (Segment 3)
   ├── claims.verified_by       (Segment 4)
   ├── matches.reviewed_by      (Segment 3/4)
   └── audit_log.actor_user_id  (everybody)
```

That is why you must **never store a name as text** in your tables — store the
`user_id` and join to `users`. The moment you store "Ayesha Rahman" as text,
the "who reported this" screen breaks the day a student changes their name.

---

## 7. Events — the loose connection

Segment 1 publishes these events. Subscribe to them if you need to react
without being called directly:

| Event | Published when | Typical subscriber |
|---|---|---|
| `user.registered` | a student signs up | Segment 2 (nothing to do yet) |
| `user.login.success` | a login succeeds | Segment 4 (audit) |
| `user.login.failed` | a login fails | Segment 4 (lock the account) |
| `user.logout` | the user signs out | Segment 2 (close the draft) |
| `user.status.changed` | an admin suspends/restore | Segment 3 (hide their claims) |
| `user.role.changed` | an admin changes a role | everybody (reload the navigation) |

```java
services.eventBus().subscribe(Events.USER_REGISTERED, event -> {
    Long userId = event.getLong("targetUserId");
    ...
});
```

Your segment may publish its own events too — use the same `AppEvent` class
and add the names to `core/event/Events.java`.

---

## 8. Branch and merge order (this saves the whole team pain)

```
main
 ├── feature/segment1-core        (model, db, services)      ← merge first
 ├── feature/segment1-contracts   (core/spi, core/event)      ← merge SECOND, before anybody else writes code
 ├── feature/segment2-items
 ├── feature/segment3-matching
 ├── feature/segment4-admin
 └── feature/integration
```

Merge order:

1. **Segment 1 core** → `main`
2. **Segment 1 contracts** → `main` ← *the others must branch from here*
3. Segment 2 → `main`
4. Segment 3 → `main`
5. Segment 4 → `main`
6. Integration day: everybody merges, run the self-test, demo

**One rule:** never merge a change that touches `core/spi`, `core/model` or
`sql/01_schema.sql` without telling the other three. Those three files are the
contract. Everything else is yours.

---

## 9. When you need something from Segment 1

Do **not** wait for me to add it. Do this:

1. Write down what you need in one sentence.
2. Write the interface method you want (2 lines).
3. Ask in the group chat: "Guys, I need `List<ItemSummary> itemsOf(long userId)` in
   `ItemCatalogService` — is that OK for everyone?"
4. If nobody objects within a day, add it to `core/spi/ItemCatalogService.java`
   yourself and mention it in the chat.

That way the contract stays alive without Segment 1 becoming a bottleneck.

---

## 10. The integration day checklist

- [ ] Every segment registers its service in `AppServices` / its own bootstrap
- [ ] `SelfTest` passes (29 checks)
- [ ] The dashboard shows real numbers (not zeros) for items, claims and matches
- [ ] A student can report an item and see it on the dashboard
- [ ] A claim can be submitted, verified and approved end to end
- [ ] A non-admin user cannot reach any admin screen (check the URL/button, not just the service)
- [ ] The audit log shows the full history of the demo
