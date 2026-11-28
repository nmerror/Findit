-- ============================================================================
--  FindIt - Campus Lost & Found Management System
--  Database schema (MySQL 8, section 9 of the project proposal)
--
--  OWNERSHIP OF THE TABLES
--    users            -> Segment 1 (you)          Authentication & RBAC
--    audit_log        -> Segment 1 (you)          Audit trail
--    locations        -> Segment 2                Campus locations
--    items            -> Segment 2                Lost & found reports
--    item_images      -> Segment 2                Photos / evidence
--    matches          -> Segment 3                Matching results
--    claims           -> Segment 3 + Segment 4    Ownership claims
--    notifications    -> Segment 1 (optional)     Dashboard alerts
--    password_resets  -> Segment 1                PIN codes for the password reset
--
--  RULE FOR THE WHOLE TEAM: a segment may only INSERT/UPDATE its own tables.
--  Cross-segment data is read through the tables above (with foreign keys),
--  never by writing into somebody else's table.
-- ============================================================================

CREATE DATABASE IF NOT EXISTS findit CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE findit;

-- Drop in reverse dependency order so the script can be re-run safely.
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS password_resets;
DROP TABLE IF EXISTS claims;
DROP TABLE IF EXISTS matches;
DROP TABLE IF EXISTS item_images;
DROP TABLE IF EXISTS items;
DROP TABLE IF EXISTS locations;
DROP TABLE IF EXISTS audit_log;
DROP TABLE IF EXISTS users;

-- ---------------------------------------------------------------------------
-- Segment 1 - users
-- ---------------------------------------------------------------------------
CREATE TABLE users (
    user_id        INT AUTO_INCREMENT PRIMARY KEY,
    full_name      VARCHAR(120)  NOT NULL,
    email          VARCHAR(160)  NOT NULL,
    student_id     VARCHAR(20)   NOT NULL,
    password_hash  VARCHAR(255)  NOT NULL,
    password_salt  VARCHAR(64)   NOT NULL,
    role           ENUM('STUDENT', 'ADMIN') NOT NULL DEFAULT 'STUDENT',
    status         ENUM('ACTIVE', 'PENDING', 'SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
    phone          VARCHAR(20)   NULL,
    avatar_image   VARCHAR(255)  NULL,
    last_login     TIMESTAMP     NULL,
    created_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uq_users_email      UNIQUE (email),
    CONSTRAINT uq_users_student_id UNIQUE (student_id),
    INDEX idx_users_role   (role),
    INDEX idx_users_status (status)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------------
-- Segment 1 - audit_log
-- ---------------------------------------------------------------------------
CREATE TABLE audit_log (
    audit_id      INT AUTO_INCREMENT PRIMARY KEY,
    actor_user_id INT          NULL,
    action        VARCHAR(60)  NOT NULL,
    details       VARCHAR(500) NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_audit_user FOREIGN KEY (actor_user_id) REFERENCES users (user_id)
        ON DELETE SET NULL
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------------
-- Segment 1 (optional) - notifications
-- ---------------------------------------------------------------------------
CREATE TABLE notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT          NOT NULL,
    title           VARCHAR(160) NOT NULL,
    message         VARCHAR(500) NULL,
    is_read         TINYINT(1)   NOT NULL DEFAULT 0,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE CASCADE,
    INDEX idx_notification_user_read (user_id, is_read)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------------
-- Segment 2 - locations
-- ---------------------------------------------------------------------------
-- Password reset: the PIN code that the user types to set a new password.
CREATE TABLE password_resets (
    reset_id    INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT         NOT NULL,
    pin_code    VARCHAR(6)  NOT NULL,
    expires_at  TIMESTAMP   NOT NULL,
    used_at     TIMESTAMP   NULL,
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_reset_user FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE CASCADE,
    INDEX idx_reset_user (user_id)
) ENGINE = InnoDB;

CREATE TABLE locations (
    location_id   INT AUTO_INCREMENT PRIMARY KEY,
    location_name VARCHAR(120) NOT NULL,
    building      VARCHAR(120) NULL,
    floor         VARCHAR(20)  NULL,
    latitude      DECIMAL(10, 7) NULL,
    longitude     DECIMAL(10, 7) NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_locations_name UNIQUE (location_name)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------------
-- Segment 2 - items
-- ---------------------------------------------------------------------------
CREATE TABLE items (
    item_id              INT AUTO_INCREMENT PRIMARY KEY,
    reported_by          INT          NOT NULL,
    item_type            ENUM('LOST', 'FOUND') NOT NULL,
    category             VARCHAR(40)  NOT NULL,
    title                VARCHAR(160) NOT NULL,
    description          TEXT         NULL,
    brand                VARCHAR(80)  NULL,
    model_name           VARCHAR(80)  NULL,
    color                VARCHAR(60)  NULL,
    identifying_marks    TEXT         NULL,
    location_id          INT          NULL,
    last_seen_location   VARCHAR(160) NULL,
    status               ENUM('OPEN', 'MATCHED', 'CLAIMED', 'RETURNED', 'CLOSED')
                         NOT NULL DEFAULT 'OPEN',
    is_urgent            TINYINT(1)   NOT NULL DEFAULT 0,
    reward_offered       VARCHAR(50)  NULL,
    lost_date            DATE         NULL,
    created_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_items_user     FOREIGN KEY (reported_by) REFERENCES users (user_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_items_location FOREIGN KEY (location_id) REFERENCES locations (location_id)
        ON DELETE SET NULL,
    INDEX idx_items_type     (item_type),
    INDEX idx_items_status   (status),
    INDEX idx_items_category (category),
    INDEX idx_items_reporter (reported_by)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------------
-- Segment 2 - item_images  (photos of the item + evidence photos of a claim)
-- ---------------------------------------------------------------------------
CREATE TABLE item_images (
    image_id   INT AUTO_INCREMENT PRIMARY KEY,
    item_id    INT         NOT NULL,
    image_type ENUM('ITEM', 'EVIDENCE', 'AVATAR') NOT NULL DEFAULT 'ITEM',
    file_path  VARCHAR(255) NOT NULL,
    uploaded_by INT        NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_images_item    FOREIGN KEY (item_id) REFERENCES items (item_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_images_uploader FOREIGN KEY (uploaded_by) REFERENCES users (user_id)
        ON DELETE CASCADE
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------------
-- Segment 3 - matches  (lost item <-> found item pairs with a score)
-- ---------------------------------------------------------------------------
CREATE TABLE matches (
    match_id    INT AUTO_INCREMENT PRIMARY KEY,
    lost_item_id INT         NOT NULL,
    found_item_id INT        NOT NULL,
    score       DECIMAL(5, 2) NOT NULL,
    status      ENUM('NEW', 'VIEWED', 'CONFIRMED', 'REJECTED') NOT NULL DEFAULT 'NEW',
    algorithm   VARCHAR(60) NULL,
    reviewed_by INT         NULL,
    matched_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_match_lost   FOREIGN KEY (lost_item_id) REFERENCES items (item_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_match_found  FOREIGN KEY (found_item_id) REFERENCES items (item_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_match_reviewer FOREIGN KEY (reviewed_by) REFERENCES users (user_id)
        ON DELETE SET NULL,
    INDEX idx_match_status (status),
    INDEX idx_match_score  (score)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------------
-- Segment 3 + Segment 4 - claims
-- ---------------------------------------------------------------------------
CREATE TABLE claims (
    claim_id           INT AUTO_INCREMENT PRIMARY KEY,
    item_id            INT         NOT NULL,
    claimant_id        INT         NOT NULL,
    match_id           INT         NULL,
    description_of_item TEXT       NULL,
    proof_details      TEXT        NULL,
    status             ENUM('SUBMITTED', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'RETURNED')
                       NOT NULL DEFAULT 'SUBMITTED',
    verified_by        INT         NULL,
    decision_note      TEXT        NULL,
    submitted_at       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    decided_at         TIMESTAMP   NULL,

    CONSTRAINT fk_claim_item     FOREIGN KEY (item_id) REFERENCES items (item_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_claim_claimant FOREIGN KEY (claimant_id) REFERENCES users (user_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_claim_match    FOREIGN KEY (match_id) REFERENCES matches (match_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_claim_verifier FOREIGN KEY (verified_by) REFERENCES users (user_id)
        ON DELETE SET NULL,
    INDEX idx_claim_status (status),
    INDEX idx_claim_item   (item_id),
    INDEX idx_claim_user   (claimant_id)
) ENGINE = InnoDB;
