# Plan — simplifying the interface

> **Status:** **executed** — see the result table in section 8.
> **Goal:** the application should look like a clean, normal admin panel — flat, readable,
> no more "designed". The backend stays exactly as it is.
> **Owner:** Sadikul Basar (20255103159) — Segment 1.
>
> **Decisions taken (locked):** flatten in place, **light mode only** (the dark
> palette and the switch are removed) and the sidebar entries of Segments 2/3/4
> are **hidden** — the other segments add their own screens when they are ready.

---

## 1. Where we are today

| | |
|---|---|
| UI code | **6 668 lines** in 31 files |
| — of which the design system (`ui/common/`) | 2 200 lines, 22 classes |
| — of which the screens | 3 240 lines, 9 files |
| Backend | complete and verified: **43/43** memory checks, **49/49** checks against a live MySQL |

The interface is not *wrong* — it is **too much**. The decoration is what costs the
most code and buys the least:

| Decoration | Where | What it costs |
|---|---|---|
| Deep-space gradient + glowing orbs | `GradientBackdrop` | 65 lines, makes the login screen the busiest thing in the app |
| Gradient cards + halo | `RoundedPanel` | every card has a gradient and a glow |
| Glowing buttons | `GlowButton` | custom painting for a hover effect |
| Luminous focus rings | `FieldSkin`, `ModernTextField`, `ModernPasswordField` | 260 lines for two input fields |
| Donut chart | `DonutChart` | 114 lines for one ring |
| Gradient bars, gradient stat cards, gradient pills | `BarList`, `StatCard`, `StatusBadge` | colour noise |
| Gradient title bar with glow | `BaseFrame` | ~40 lines of painting |

---

## 2. The recommended path: **flatten in place**

Keep the architecture, strip the decoration. The screens keep their layout, their
logic and their behaviour — only the painting changes.

**Result:** a flat admin panel — solid backgrounds, 1-pixel borders, one accent
colour, a normal table, plain bars. Dark **and** light both still work, because
the palette lives in `UiTheme` and the screens rebuild themselves on a switch.

### 2.1 The design system — 22 classes → 14

| Action | Class | What changes |
|---|---|---|
| **delete** | `GradientBackdrop` | the login screen gets a plain solid background |
| **delete** | `DonutChart` | the claim pipeline becomes a thin stacked bar (new, ~40 lines) |
| **rewrite** | `RoundedPanel` | solid fill + 1 px border, no gradient, no halo |
| **rewrite** | `GlowButton` → renamed `AppButton` | flat button, one accent colour, subtle hover |
| **rewrite** | `FieldSkin`, `ModernTextField`, `ModernPasswordField` | plain bordered input, keep the placeholder |
| **rewrite** | `StatCard` | caption + number only, no glyph badge, no gradient |
| **rewrite** | `BarList` | flat bars, one colour per state |
| **rewrite** | `BaseFrame` | flat title bar: solid colour, 1 px bottom border, no glow |
| **rewrite** | `ModernTable` | plain header, thin row separators, no stripes |
| **rewrite** | `UiTheme` | fewer colours: neutrals + one accent + four semantic states |
| **tweak** | `NavButton`, `StatusBadge`, `AvatarLabel`, `PlaceholderPanel`, `Icons`, `Fonts`, `ThemeToggle`, `StretchPanel` | colours only, no behaviour change |

### 2.2 The screens — same structure, flat styling

| Screen | What stays | What changes |
|---|---|---|
| Login | validation, the two fields, the sign-in button | centred card on a plain background; the three marketing bullets go |
| Register | validation and the field list | plain form |
| Dashboard | 4 KPI cards, location bars, recent-items table | flat cards; the donut becomes a thin stacked bar |
| Manage Users | table, suspend/restore/role actions | flat table, plain buttons |
| My Profile | account card, password change, activity feed | flat |
| Audit Log | table + the "clear old entries" action | flat |
| Search Results | search box + result list | flat |
| Reports | report text, export .txt / .csv | flat |

### 2.3 What is **not** touched

* The backend (`db/`, `service/`, `core/`) — **not one line changes**.
* The seven service contracts and the registration line your teammates use.
* RBAC, sessions, the audit trail, the search, the report, the theme switch.
* The other segments' placeholder screens — their content is not my segment.

---

## 3. Why not the other two paths

| Path | Why not |
|---|---|
| **Rewrite the screens with stock Swing widgets** (JTable, JButton, Nimbus) | It is the most "normal" looking, but Swing's look-and-feel is light-only — the dark mode we built would have to be thrown away, and every screen's layout would need re-testing. Two days of work for a worse result. |
| **Flatten + drop dark mode** | The dark/light switch is already built, already tested and already documented. Keeping it costs nothing once the painting is flat, and it is the thing that makes the demo look deliberate. |

---

## 4. The plan of work

| Step | What | Time |
|---|---|---|
| 1 | Flatten the design system (the table in 2.1) | ~3 h |
| 2 | Flatten the eight screens (2.2) | ~3 h |
| 3 | Simplify the palette in `UiTheme` | ~1 h |
| 4 | Before/after screenshots of every screen in both modes | ~1 h |
| 5 | Re-run both test suites + the live-database check | ~30 min |
| 6 | Update the README, the file index and the screenshots in the docs | ~1 h |

**Total:** about a day. The branch is `feature/clean-ui`, opened as a pull request
like the others.

---

## 5. How we know it worked

| Acceptance check | How |
|---|---|
| Nothing broke | `SelfTest` 43/43 **and** `BackendSelfTest` 49/49 against a live MySQL |
| The behaviour is unchanged | log in as student and as admin, open every screen, run a search, export a report, switch the theme |
| It really looks simpler | before/after screenshots side by side, both palettes |
| The code got smaller | the UI shrinks from ~6 700 to ~4 000 lines, and from 22 design classes to 14 |

---

## 6. The two decisions (locked)

1. **How far to go** — *flatten in place* and **light mode only**. The dark
   palette, the sun/moon button and the `app.theme` property are removed, so
   the application is one light theme from the first start.
2. **The sidebar placeholders** — *hidden*. The navigation lists only the
   screens that really exist. The other segments add theirs with one call:
   `mainFrame.addSegmentSection(section, item, cardName, panel)`
   (documented in `docs/INTEGRATION_CONTRACT.md`).

---

## 7. One note on the backend

The backend is **done and verified** — nothing in this plan changes it. What is
still open is not mine:

* Segments 2, 3 and 4 have their **tables** and their **reference
  implementations**, but their **screens** are still placeholders.
* The reference implementations in `service/data/` read the real tables, so the
  dashboard already shows real numbers. When a teammate registers their own
  implementation, theirs wins — nothing else changes.

---

## 8. What was actually done

| Item | Result |
|---|---|
| `GradientBackdrop`, `DonutChart`, `ThemeToggle`, `PlaceholderPanel` | **deleted** |
| `GlowButton` | **renamed** to `AppButton` and flattened (PRIMARY / SECONDARY / DANGER / SUBTLE) |
| `StackedBar` | **new** (~100 lines) — the claim pipeline is now a flat bar with a legend |
| `UiTheme` | one light palette, fewer colours, and it installs **Nimbus** + the colours of the standard widgets |
| `BaseFrame` | normal window decorations, no custom title bar, no theme switch |
| `RoundedPanel`, `FieldSkin`, `ModernTextField`, `ModernPasswordField`, `StatCard`, `BarList`, `ModernTable`, `NavButton`, `TopBar`, `StatusBadge`, `AvatarLabel` | flat, no gradients, no glows |
| `Fonts` | the font lookup had a case bug and never matched a family — it now uses the system interface font (`Font.SANS_SERIF`) |
| `AppConfig` | `theme()` and `saveTheme()` removed, `app.theme` removed from `app.properties` |
| The login screen | one centred card, the hero column and the marketing bullets are gone |
| The dashboard | the donut became the stacked bar; the rows now stretch over the full width (the BoxLayout alignment was fixed) |
| The navigation | only the screens of Segment 1 — the segments 2/3/4 add theirs with `addSegmentSection(...)` |
| The backend | **not one line changed** |
| Tests | `SelfTest` **43/43**, `BackendSelfTest` **49/49** against a live MySQL |
