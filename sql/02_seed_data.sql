-- ============================================================================
--  FindIt - seed data
--  Run AFTER sql/01_schema.sql:  mysql -u root -p < sql/02_seed_data.sql
--
--  The two accounts below are ready to use. Their passwords are hashed with
--  PBKDF2WithHmacSHA256 (the same algorithm the application uses).
--
--    administrator : admin@findit.edu / Admin@1234
--    student       : ayesha@buet.edu / Student@123
--
--  Change these passwords before you present the project.
--  To create a hash for a new password run:
--     java -cp target/classes com.bubt.findit.tools.HashTool <password>
-- ============================================================================

USE findit;

-- ---------------------------------------------------------------------------
-- Users
-- ---------------------------------------------------------------------------
INSERT INTO users (full_name, email, student_id, password_hash, password_salt, role, status, phone)
VALUES ('System Administrator', 'admin@findit.edu', 'ADMIN001',
        'SxPQxIsrNQh7HftDM3i6/Ew0Q85UrHGTMi3Z+YseGco=', 'dyqyonfWyZBcuKe2V1OeGw==',
        'ADMIN', 'ACTIVE', '01700000000');

INSERT INTO users (full_name, email, student_id, password_hash, password_salt, role, status, phone)
VALUES ('Ayesha Rahman', 'ayesha@buet.edu', '21-04234',
        'ADxTiiGd7JB6/rLySTTsPqu8uRcXpFfFnlUuMqZxndM=', 'TrfHAzHS+ixE4Yl362Xqhw==',
        'STUDENT', 'ACTIVE', '01800000000');

INSERT INTO users (full_name, email, student_id, password_hash, password_salt, role, status, phone)
VALUES ('Tanvir Ahmed', 'tanvir@buet.edu', '21-04311',
        'ADxTiiGd7JB6/rLySTTsPqu8uRcXpFfFnlUuMqZxndM=', 'TrfHAzHS+ixE4Yl362Xqhw==',
        'STUDENT', 'ACTIVE', '01900000000');

-- ---------------------------------------------------------------------------
-- Locations (Segment 2)
-- ---------------------------------------------------------------------------
INSERT INTO locations (location_name, building, floor) VALUES
    ('Central Library', 'Library', '2'),
    ('Main Auditorium', 'Academic Building', 'G'),
    ('Cafeteria', 'Student Center', '1'),
    ('Exam Hall', 'Academic Building', '3'),
    ('Hostel Gate', 'Residential Area', 'G'),
    ('Sports Complex', 'Sports Zone', 'G');

-- ---------------------------------------------------------------------------
-- Sample items (Segment 2) - reported_by = 2 (Ayesha), 3 (Tanvir)
-- ---------------------------------------------------------------------------
INSERT INTO items (reported_by, item_type, category, title, description, brand, model_name,
                   color, identifying_marks, location_id, last_seen_location, status, is_urgent, lost_date)
VALUES (2, 'LOST', 'ACCESSORIES', 'Black leather wallet',
        'Wallet with three cards and some cash inside', 'Wildcraft', null,
        'Black', 'Small scratch on the front flap', 1, 'Central Library', 'OPEN', 1, CURRENT_DATE - INTERVAL 2 DAY);

INSERT INTO items (reported_by, item_type, category, title, description, brand, model_name,
                   color, identifying_marks, location_id, last_seen_location, status, is_urgent, lost_date)
VALUES (3, 'LOST', 'ELECTRONICS', 'Blue PS4 controller',
        'DualShock controller with a charging cable', 'Sony', 'DualShock 4',
        'Blue', 'Sticker of a star on the back', 4, 'Exam Hall', 'OPEN', 0, CURRENT_DATE - INTERVAL 5 DAY);

INSERT INTO items (reported_by, item_type, category, title, description, brand, model_name,
                   color, identifying_marks, location_id, last_seen_location, status, is_urgent, lost_date)
VALUES (2, 'FOUND', 'DOCUMENTS', 'Student ID card',
        'Found near the cafeteria entrance', null, null,
        null, 'Name printed on the front', 3, 'Cafeteria', 'OPEN', 0, CURRENT_DATE - INTERVAL 1 DAY);

INSERT INTO items (reported_by, item_type, category, title, description, brand, model_name,
                   color, identifying_marks, location_id, last_seen_location, status, is_urgent, lost_date)
VALUES (3, 'FOUND', 'ELECTRONICS', 'Black wireless earphones',
        'Found on a bench in the library', 'boAt', 'Airdopes 141',
        'Black', 'Charging case is slightly scratched', 1, 'Central Library', 'OPEN', 0, CURRENT_DATE - INTERVAL 1 DAY);

-- ---------------------------------------------------------------------------
-- Sample match (Segment 3): the lost wallet vs. nothing yet, plus a
-- lost controller vs. a found controller would be added here.
-- ---------------------------------------------------------------------------
INSERT INTO matches (lost_item_id, found_item_id, score, status, algorithm)
VALUES (2, 4, 71.50, 'NEW', 'token-overlap-v1');

-- ---------------------------------------------------------------------------
-- Sample claim (Segment 3 / Segment 4)
-- ---------------------------------------------------------------------------
INSERT INTO claims (item_id, claimant_id, match_id, description_of_item, proof_details,
                    status, submitted_at)
VALUES (3, 3, NULL,
        'My student ID card, white with a blue strip and my photo',
        'The student number is printed at the bottom',
        'SUBMITTED', CURRENT_TIMESTAMP - INTERVAL 4 HOUR);
