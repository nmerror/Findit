# FindIt - Campus Lost and Found System

**FindIt** is a desktop application (Java + Swing + MySQL) for the campus lost and found: students report what they lost or found, the system matches the two, and an administrator verifies the claim and confirms the return.

This repository is developed by the Advanced Programming Lab (CSE 342) team, BUBT, intake 55-4.

| Member | ID | Role | Responsibility |
|---|---|---|---|
| MD. Nurul Azom Arif | 20255103126 | Leader | Project management and coordination |
| Muntasir Al Simum | 20255103134 | Co-Leader | Segment 3 - matching engine and claims |
| Rajmohan Chandra Shil | 20255103149 | Co-Leader | Segment 2 - item reporting, images and locations |
| **Sadikul Basar** | **20255103159** | **Co-Leader** | **Segment 1 - authentication, RBAC and dashboard** |

## How the work is organised

The project is split into four segments that plug into one another through service contracts (`core/spi`) and an event bus (`core/event`). Every segment owns its own tables, and the shared `users` table joins them together.

| Document | What it explains |
|---|---|
| `docs/PROPOSAL_TRACEABILITY.md` | Which part of the project proposal belongs to whom |
| `docs/INTEGRATION_CONTRACT.md` | What each segment must implement to plug in |
| `docs/SEGMENT1_FILE_INDEX.md` | What every single file does |
| `docs/SEGMENT1_DESIGN.md` | Why Segment 1 is built this way |
| `docs/START_HERE.md` | The day-by-day build order |

The Segment 1 foundation is on the branch `feature/segment1-foundation`.
