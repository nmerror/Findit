# Backend Design — how FindIt stores its data

> The part you need when you want to change something in the database layer or
> add a table of your own. Read this once, then look at
> `docs/INTEGRATION_CONTRACT.md` for what your segment must implement.

---

## 1. The layers

```
  ui/            Swing screens - never touch the database directly
    |
  service/       business rules (no Swing code, so they can be tested)
    |
  core/spi/      the contracts between the segments
    |
  db/dao/        one DAO per table - SQL lives here and nowhere else
    |
  db/            the connection pool and the transaction helper
    |
  MySQL 8        the database
```

**The rule that keeps the merge painless:** a screen never writes SQL, a
service never knows whether the data comes from MySQL or from memory, and a
DAO never knows anything about the user interface.

---

## 2. The connection pool

`db/ConnectionPool.java` + `db/Database.java`

Opening a new TCP connection for every single query is slow and exhausts the
`max_connections` limit of a shared MySQL server. The pool keeps a small
number of connections open and hands them out on demand.

```java
Connection connection = Database.getConnection();
try {
    ...
} finally {
    Database.release(connection);
}
```

`Database.getConnection()` returns a **proxy**: calling `close()` on it (which
`try-with-resources` does automatically) returns the connection to the pool
instead of closing it. That is why the DAO code did not have to change when
the pool was added.

| Setting | Where | Default |
|---|---|---|
| `db.pool.size` | `app.properties` | 8 |
| `db.url` | `app.properties` | `jdbc:mysql://localhost:3306/findit?...` |
| `db.user` / `db.password` | `app.properties` | `root` / *(empty)* |

The pool has **no third-party dependency** — that is deliberate, so the
project still compiles with a plain `javac` and no Maven.

---

## 3. Transactions

`Database.inTransaction(...)` runs a block inside one transaction: everything
is committed when it finishes, rolled back when it throws.

```java
Database.inTransaction(connection -> {
    claimDao.insert(claim);
    auditDao.insert(entry);
    return null;
});
```

`BackendSelfTest` checks that a failing transaction really rolls back
("the rolled back row is not in the database").

---

## 4. The DAOs

One interface per table, one JDBC implementation and (where it makes sense)
one in-memory implementation.

| DAO | Table | MySQL | In memory |
|---|---|---|---|
| `UserDao` | `users` | `JdbcUserDao` | `InMemoryUserDao` |
| `AuditDao` | `audit_log` | `JdbcAuditDao` | `InMemoryAuditDao` |
| `NotificationDao` | `notifications` | `JdbcNotificationDao` | `InMemoryNotificationDao` |
| `ItemDao` | `items` + `locations` | `JdbcItemDao` | – |
| `ClaimDao` | `claims` | `JdbcClaimDao` | – |
| `MatchDao` | `matches` | `JdbcMatchDao` | – |

`DaoFactory` picks the implementation: MySQL when the app runs with a database,
the in-memory one when it does not.

**Every statement is a `PreparedStatement`.** The values are sent separately
from the SQL, so a title containing a quote can neither break the query nor
inject SQL. No string concatenation of values anywhere in `db/`.

### The write-side records

Reading and writing want different shapes, so there are two kinds of beans:

| Bean | Used for |
|---|---|
| `core/model/ItemSummary`, `LocationCount` | reading (the dashboard view) |
| `db/dao/ItemRecord`, `ClaimRecord`, `MatchRecord` | writing (the full row) |

---

## 5. The five database operations of Section 9

| Operation | Where |
|---|---|
| **INSERT** | `JdbcUserDao.insert()`, `JdbcAuditDao.insert()`, `JdbcNotificationDao.insert()`, `JdbcItemDao.insert()`, `JdbcClaimDao.insert()`, `JdbcMatchDao.insert()` |
| **SELECT** | every `find…` / `recent…` / `count…` method |
| **UPDATE** | `updatePassword`, `updateStatus`, `updateRole`, `updateLastLogin`, `JdbcClaimDao.updateStatus()` |
| **DELETE** | `JdbcAuditDao.deleteOlderThan()` ("clear entries older than 30 days"), `UserDao.deleteById()` |
| **SEARCH / FILTER** | `SearchService` + the `WHERE` clauses of the DAOs |
| **JOIN / REPORT** | `JdbcItemDao.recent()` (items ⋈ users ⋈ locations), `JdbcItemDao.topLocations()` (GROUP BY), `ReportService` |

---

## 6. The schema

`sql/01_schema.sql` creates the **whole** project database — eight tables with
foreign keys, unique constraints and indexes:

| Table | Owner | Purpose |
|---|---|---|
| `users` | Segment 1 | accounts, roles, password hashes |
| `audit_log` | Segment 1 | every important action |
| `notifications` | Segment 1 | in-app alerts |
| `locations` | Segment 2 | campus places |
| `items` | Segment 2 | lost & found reports |
| `item_images` | Segment 2 | photos and evidence |
| `matches` | Segment 3 | lost ↔ found pairs with a score |
| `claims` | Segment 3 + 4 | ownership claims and their verification |

`sql/02_seed_data.sql` fills it with three accounts, six locations, four
items, one match and one claim — enough for the dashboard and the report to
show real numbers.

Load them with:

```bash
mysql -u root -p < sql/01_schema.sql
mysql -u root -p < sql/02_seed_data.sql
```

---

## 7. Which data the dashboard shows

`DashboardService` only asks the **contracts**, never the tables:

| Dashboard card | Comes from | Read by |
|---|---|---|
| Lost items / Found items | `ItemCatalogService` | Segment 2's tables |
| Open claims / pending / approved / returned | `ClaimQueryService` | Segment 3's tables |
| New & confirmed matches | `MatchQueryService` | Segment 3's tables |
| Busiest locations | `ItemCatalogService.topLocations()` | `GROUP BY` on `items` + `locations` |
| Recently reported items | `ItemCatalogService.recentItems()` | `items` ⋈ `users` ⋈ `locations` |
| Registered users / students / admins | `users` table | Segment 1 |

With a database attached, the four contracts are the database-backed classes
in `service/data/`. They are **reference implementations**: the owning segment
can replace any of them with their own class, and nothing else has to change.

---

## 8. Two ways to run

| Mode | Command | What it uses |
|---|---|---|
| Real database | `bash run.sh --mysql` | MySQL 8 + JDBC, every table |
| Demo / no database | `bash run.sh` | in-memory DAOs + fake segment data |

The launcher picks the driver from `lib/` (fetched by `setup-java.sh`).

---

## 9. The backend self-test

```bash
java -cp "target/classes:target/test-classes:lib/*" com.bubt.findit.BackendSelfTest
```

**49 checks** against the real database: users, audit trail, notifications,
items, matches, claims, the contracts, transactions and the pool. It writes
its own rows (prefix `BACKEND-TEST-`) and removes them again.

It found three real bugs while it was being written:

* `JdbcUserDao` asked for a column called `posted_by` — the schema calls it
  `reported_by` (the in-memory DAO never noticed).
* `ItemCatalogServiceImpl` sent the *display label* ("Lost Item") to the
  database instead of the stored value (`LOST`).
* The dashboard still showed the demo data when a real database was attached.

**Run it after you change anything in `db/`.**

---

## 10. Adding a table of your own

1. Add the table to `sql/01_schema.sql` **inside your own section** (and add
   the `DROP TABLE IF EXISTS` line at the top, in reverse dependency order).
2. Write `db/dao/YourDao.java` (the interface) and
   `db/dao/JdbcYourDao.java` (the MySQL implementation). Copy
   `JdbcItemDao` — the style is the same everywhere.
3. Add it to `DaoFactory`.
4. Publish what the dashboard needs through **your** contract in `core/spi/`.
   Never let Segment 1 read your table directly.
