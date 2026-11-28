-- ============================================================================
--  FindIt - seed data
--  Run AFTER sql/01_schema.sql:  mysql -u root -p < sql/02_seed_data.sql
--
--  This file contains the ACCOUNTS and the LOCATIONS only - no lost items.
--  FindIt is a real system: the dashboard starts at zero and every lost item
--  on the screen was posted by a real user through the application.
--
--    administrator : admin@findit.edu / Admin@1234
--    student       : ayesha@buet.edu / Student@123
--    student       : tanvir@buet.edu / Student@123
--
--  The passwords are hashed with PBKDF2WithHmacSHA256 - the same algorithm the
--  application uses. Change these passwords before you present the project.
--  To create a hash for a new password run:
--     java -cp target/classes com.bubt.findit.tools.HashTool <password>
-- ============================================================================

USE findit;

-- ---------------------------------------------------------------------------
-- Users
-- ---------------------------------------------------------------------------
INSERT INTO users (full_name, email, student_id, password_hash, password_salt, role, status, phone)
VALUES ('System Administrator', 'admin@findit.edu', 'ADMIN001',
        'XHTWKXE6Y5MNWqPZhwPr39+0e3/tSAN++2ZWarrRBaE=', 'Y8cThmzZ8CzeqqEdKNAyRw==',
        'ADMIN', 'ACTIVE', '01700000000');

INSERT INTO users (full_name, email, student_id, password_hash, password_salt, role, status, phone)
VALUES ('Ayesha Rahman', 'ayesha@buet.edu', '21-04234',
        'wkYbNLjHbVdzSoxc/JXBR+0RYq1J9UlcdKX0xVc9Ct4=', 'fx2hCQP1om1a4xfiL5p05A==',
        'STUDENT', 'ACTIVE', '01800000000');

INSERT INTO users (full_name, email, student_id, password_hash, password_salt, role, status, phone)
VALUES ('Tanvir Ahmed', 'tanvir@buet.edu', '21-04311',
        'b/SklR6amMxq1ZdsbKsBmHBWrUGVRa5ze7j8pfcARJk=', 'ELVEra3ouXkI2Ewdd0PsOw==',
        'STUDENT', 'ACTIVE', '01900000000');

-- ---------------------------------------------------------------------------
-- Locations - the places of the campus. They are reference data, not posts.
-- ---------------------------------------------------------------------------
INSERT INTO locations (location_name, building, floor) VALUES
    ('Central Library', 'Library', '2'),
    ('Main Auditorium', 'Academic Building', 'G'),
    ('Cafeteria', 'Student Center', '1'),
    ('Exam Hall', 'Academic Building', '3'),
    ('Hostel Gate', 'Residential Area', 'G'),
    ('Sports Complex', 'Sports Zone', 'G');
