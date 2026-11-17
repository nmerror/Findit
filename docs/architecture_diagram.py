"""
Generates docs/images/architecture.png - the diagram that shows how the four
FindIt segments connect to each other.

Run with:  python3 docs/architecture_diagram.py
"""

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch, FancyArrowPatch

S1 = "#1E5F8C"   # Segment 1 blue
S2 = "#2E8B57"   # Segment 2 green
S3 = "#B5651D"   # Segment 3 orange
S4 = "#8E44AD"   # Segment 4 purple
CORE = "#C08A2E"  # shared core gold
BG = "#FFFFFF"
GREY = "#5A6B7B"

fig, ax = plt.subplots(figsize=(15.5, 9.6), dpi=130)
ax.set_xlim(0, 100)
ax.set_ylim(0, 62)
ax.axis("off")
fig.patch.set_facecolor(BG)


def box(x, y, w, h, text, color, fill="#FFFFFF", fs=11, bold=False, radius=1.2):
    ax.add_patch(FancyBboxPatch(
        (x, y), w, h,
        boxstyle=f"round,pad=0.0,rounding_size={radius}",
        linewidth=2.0, edgecolor=color, facecolor=fill, zorder=2))
    if text:
        ax.text(x + w / 2, y + h / 2, text, ha="center", va="center",
                fontsize=fs, color=color, zorder=3,
                fontweight="bold" if bold else "normal", linespacing=1.5)


def arrow(x1, y1, x2, y2, label="", color=GREY, ls="-", lw=1.8, rad=0.0, lx=None, ly=None, z=5):
    ax.add_patch(FancyArrowPatch(
        (x1, y1), (x2, y2), arrowstyle="-|>", linestyle=ls, linewidth=lw,
        color=color, mutation_scale=15, zorder=z,
        connectionstyle=f"arc3,rad={rad}"))
    if label:
        ax.text(lx if lx is not None else (x1 + x2) / 2,
                ly if ly is not None else (y1 + y2) / 2 + 1.4,
                label, ha="center", va="center", fontsize=9.5, color=color, zorder=4,
                bbox=dict(boxstyle="round,pad=0.25", fc=BG, ec="none"))


# ---------------------------------------------------------------- title
ax.text(50, 59.5, "FindIt  —  how the four segments connect",
        ha="center", va="center", fontsize=19, fontweight="bold", color="#22303C")
ax.text(50, 57.2,
        "Segment 1 owns the shared nouns and the contracts.  Segments 2, 3 and 4 implement "
        "the contracts.  Nobody imports anybody else's code.",
        ha="center", va="center", fontsize=11, color=GREY)

# ------------------------------------------------------- 1. UI layer
box(4, 47.5, 92, 7.2,
    "PRESENTATION LAYER   (Swing)\n"
    "Segment 1: Login · Registration · Dashboard · Profile · Manage Users · Audit Log\n"
    "Segments 2/3/4: their screens are panels inside the same window — one line in MainFrame",
    S1, fill="#F2F7FB", fs=11)

# -------------------------------------------------- 2. service layer
box(4, 37.0, 92, 7.4,
    "SERVICE LAYER   (business rules, no Swing code)\n"
    "Segment 1: AuthService · RbacService · UserService · SessionManager · DashboardService\n"
    "Segments 2/3/4: their own services, each guarded by rbac.require(user, Permission.X)",
    S1, fill="#F2F7FB", fs=11)

# ------------------------------------------------------- 3. the core
box(4, 14.0, 55, 19.0, "", CORE, fill="#FDF6E7")
ax.text(31.5, 31.2, "CORE  (shared package — everyone may use it)",
        ha="center", va="center", fontsize=12, fontweight="bold", color=CORE)

box(6, 24.1, 25, 6.2,
    "core.model\nUser · Role · Permission\nSession · DashboardStats", CORE, fs=9.5)
box(33, 24.1, 24, 6.2,
    "core.spi   ← the contracts\nItemCatalogService\n"
    "ClaimQueryService · MatchQueryService\nAdminWorkloadService", CORE, fs=9)
box(6, 15.5, 25, 5.4, "core.event\nEventBus · AppEvent · Events", CORE, fs=9.5)
box(33, 15.5, 24, 5.4, "core.registry\nServiceRegistry  (the wiring hub)", CORE, fs=9.5)

# ------------------------------------------- 4. the other three segments
box(63, 14.0, 33, 19.0, "", "#4A5568", fill="#F4F6F8")
ax.text(79.5, 31.2, "THE OTHER SEGMENTS",
        ha="center", va="center", fontsize=12, fontweight="bold", color="#4A5568")

box(65, 26.4, 29, 4.0, "Segment 2 — Item Reporting\nimplements ItemCatalogService", S2, fs=9.5)
box(65, 21.4, 29, 4.0, "Segment 3 — Matching & Claims\nimplements Claim/MatchQueryService", S3, fs=9.5)
box(65, 16.4, 29, 4.0, "Segment 4 — Admin Verification\nimplements AdminWorkloadService", S4, fs=9.5)

# ------------------------------------------------- 5. data layer
box(4, 1.5, 92, 11.0, "", "#4A5568", fill="#F4F6F8")
ax.text(50, 10.9, "DATA LAYER  —  MySQL 8 + JDBC   (one owner per table)",
        ha="center", va="center", fontsize=12, fontweight="bold", color="#4A5568")

box(6, 4.6, 20, 5.0, "users\naudit_log\nnotifications", S1, fs=10)
box(28, 4.6, 20, 5.0, "locations\nitems\nitem_images", S2, fs=10)
box(50, 4.6, 20, 5.0, "matches", S3, fs=10)
box(72, 4.6, 22, 5.0, "claims", S3, fs=10)
ax.text(83, 3.8, "also updated by Segment 4", ha="center", va="center",
        fontsize=8.5, color=GREY, style="italic")

# ------------------------------------------------------------ arrows
arrow(50, 47.5, 50, 44.4, color=S1)                 # UI -> service
arrow(50, 37.0, 50, 33.0, color=S1)                 # service -> core
arrow(50, 14.0, 50, 12.5, color=GREY, label="JDBC DAO")   # core -> data

arrow(65, 28.4, 59.5, 28.4, color=S2, ls="--")     # segment 2 -> contract
arrow(65, 23.4, 59.5, 23.4, color=S3, ls="--")     # segment 3 -> contract
arrow(65, 18.4, 59.5, 18.4, color=S4, ls="--")     # segment 4 -> contract
ax.text(61.6, 23.4, "implements", ha="center", va="center", fontsize=8.5,
        color=GREY, style="italic", rotation=90, zorder=4,
        bbox=dict(boxstyle="round,pad=0.2", fc=BG, ec="none"))

# the real join: users.user_id
arrow(16, 4.1, 60, 4.1, color="#C0392B", lw=1.5, rad=0.0, z=6)
ax.text(38, 2.6, "users.user_id  —  the actual link between all four segments",
        ha="center", va="center", fontsize=9.5, color="#C0392B",
        bbox=dict(boxstyle="round,pad=0.3", fc=BG, ec="none"), zorder=4)

plt.savefig("docs/images/architecture.png", dpi=130, facecolor=BG,
            bbox_inches="tight", pad_inches=0.25)
# SVG is a text format, so it can be committed to GitHub and still scale.
plt.savefig("docs/images/architecture.svg", format="svg", facecolor=BG,
            bbox_inches="tight", pad_inches=0.25)
print("saved docs/images/architecture.png and docs/images/architecture.svg")
