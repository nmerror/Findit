# FindIt — Project Proposal (BUBT, CSE 342)

> The filled proposal. The submission document is `FindIt_Project_Proposal.docx`;
> this file is the same content in plain text, so it can be read in the browser
> and reviewed in a pull request.

---

BANGLADESH UNIVERSITY OF BUSINESS AND TECHNOLOGY (BUBT)

Department of Computer Science and Engineering

PROJECT PROPOSAL

Advanced Programming Lab- CSE 342

# Course Information

| Particulars | Details |
|---|---|
| Course Title | Advanced Programming Lab |
| Course Code | CSE 342 |
| Semester / Intake / Section | 55-4 |
| Submission Date | 12/11/26 |

## 1. Project Information

| Particulars | Details |
|---|---|
| Proposed Project Title | FindIt: Campus Lost and Found System |
| Application Type | Desktop Application (GUI-based) |
| Programming Language | Java 17 |
| GUI Framework | Java Swing |
| Database Management System | MySQL 8 |
| Database Connectivity | JDBC (mysql-connector-j 8.4.0, PreparedStatement) |
| Development IDE | IntelliJ IDEA + Vscode |

## 2. Team Information

| No. | Student Name | Student ID | Section | Assigned Responsibility |
|---|---|---|---|---|
| 1 | MD. Nurul Azom Arif | 20255103126 | 4 | Leader |
| 2 | Muntasir Al Simum | 20255103134 | 4 | Co-Leader |
| 3 | Rajmohan Chandra Shil | 20255103149 | 4 | Co-Leader |
| 4 | Sadikul Basar | 20255103159 | 4 | Co-Leader |

## 3. Introduction and Project Background

Provide a brief overview of the proposed application. Explain the real-world context, the intended users, and the reason for developing the system.

FindIt is a desktop application that manages the lost and found office of a campus. Every day students lose wallets, phones, identity cards and notebooks in the classrooms, the library, the canteen and on the playing field. The office keeps a paper register and hangs a notice board, so a lost wallet is only returned when a student happens to read the board, and everything that nobody claims is thrown away after a few weeks.

FindIt replaces that register with a database and that notice board with a dashboard. A student reports what he has lost or what he has found, including the category, the colour, the place on campus and a photo. The system then matches the lost reports with the found ones and shows a similarity score for every pair, the office verifies the claim against the photo evidence, and the item is returned to its owner. Every step is written to an audit log.

The intended users are the students of the campus and the staff of the lost and found office. The students report their belongings and follow the status of their own claim; the staff see the whole system, verify the claims, manage the accounts and read the report. The system is developed as one Java desktop application by four members in parallel, one segment each, so the process of building it also proves that the segments fit together.

## 4. Problem Statement

Clearly describe the problem that the proposed system will address. Identify the limitations of the current/manual process and explain how a Java application will help solve the problem.

The current process is manual and it loses information. The register is a sheet of paper: it is searched by eye, a single misspelled name makes an item invisible, and there is no way to filter the sheet by category or by place. Matching a lost report with a found report depends on the memory of one staff member, so the most valuable objects are often the ones that stay unclaimed. A student cannot prove that a phone is his, because the description in the register is too weak and no photo is kept. When an item leaves the office, nobody can say who approved it, and the decision cannot be reviewed later.

A Java application solves this because the data is relational: a report belongs to a student, to a place and to a photo; a claim belongs to a report and to a student; a decision belongs to an administrator. MySQL with JDBC stores that data permanently, queries it in milliseconds and keeps it consistent, and Swing gives the office a program that runs on its own machine without a server to install and maintain.

The Java application adds what the paper process cannot do: the search and the filters over every report, the automatic comparison of the lost and the found items with a visible score, the photo evidence that makes a verification possible, the role based access control that gives the administrator more rights than a student, and the audit trail that makes every decision of the office reviewable.

## 5. Project Objectives

List the specific and achievable objectives of the proposed project.

1. To build a secure user authentication system with registration, PBKDF2 password hashing, session management and role based access control, so that every action of the application is performed by an identified and permitted user.

2. To design one MySQL database with nine tables, foreign keys and indexes that covers the whole project, and to connect it to Java with JDBC and PreparedStatement through a connection pool and real transactions.

3. To implement the complete workflow of the lost and found office - report, match, claim, verify and return - in four cooperating segments that are integrated through service contracts so that they can be developed in parallel.

4. To deliver a working, documented and automatically tested desktop application with at least four functional screens, a test suite that runs on every commit, and documentation that lets a new team member join the project in one day.

## 6. Proposed Features and Functionalities

List the major modules and describe what each module will do.

| Module / Feature | Description / Expected Functionality |
|---|---|
| User Login and Authentication | Registration and sign-in with an e-mail address or a student ID. Passwords are stored as PBKDF2 hashes with a random salt, sessions expire automatically, and the access to every operation is controlled by eleven permissions that belong to the two roles (student, administrator). Every action is written to the audit log with the user, the time and the details. Implemented in Segment 1. |
| Module 1 - Lost and Found Item Reporting, Image Upload and Location Management | A student reports an item he has lost or found with its category, brand, colour, description, campus location and a photo that later serves as the evidence of the verification. The open reports can be browsed and filtered by type, category and location, and the locations of the campus are maintained by the office. Implemented in Segment 2. |
| Module 2 - Intelligent Item Matching and Similarity Detection | The engine compares the lost reports with the found ones on category, brand, colour, place and time, calculates a similarity score for every pair and proposes the pairs that most likely belong together. The score is a suggestion - a human always makes the final decision. Implemented in Segment 3. |
| Module 3 - Ownership Claim Management, Administrative Verification and Item Return | A student claims a matched item; the office sees the claim next to the photo of the item and approves or rejects it, and the decision is written to the audit log and notified to the student. When the item is physically handed over, the return is confirmed and the status of the item becomes returned. Implemented in Segment 3 and Segment 4. |
| Search and Report Generation | One global search field over the people, the items and the claims, which the other segments join with through the same contract, and a report of the whole system - users, items, claims and audit log - that can be exported as a text file or as CSV. Implemented in Segment 1. |

## 7. GUI Design and Screens

Attach or sketch preliminary screen designs. The application must include at least four functional GUI screens, as relevant to the project.

| Proposed Screen | Purpose / Main Controls |
|---|---|
| 1. Login / Registration / Password Reset Screen | Three entry screens in black, white and grey: the sign-in card with the identifier (e-mail or student ID), the password and the inline validation; the registration form with a normal e-mail address and an optional student ID; and the forgot-password screen that sends a 6-digit PIN. Implemented in ui/auth/LoginFrame.java, ui/auth/RegisterFrame.java and ui/auth/ForgotPasswordFrame.java. |
| 2. Dashboard / Main Menu | The main window with the navigation rail, the header with the global search and the clock, and the dashboard with four KPI cards (lost posts, found posts, accounts, my posts), the table of the recent lost posts with the photo thumbnails and the table of the accounts the viewer may see. The dashboard starts at zero - FindIt ships without any fake data. Implemented in ui/MainFrame.java and ui/dashboard/DashboardPanel.java. |
| 3. Data Management Screen | Administrator only: the list of all the accounts with the buttons to suspend, to restore, to change the role and to delete the account; the list of all the lost posts with the button to remove a post; and the audit log that shows every action of every user. Implemented in ui/users/UserManagementPanel.java, ui/posts/PostManagementPanel.java and ui/users/AuditLogPanel.java. |
| 4. Search / Report Screen | The results of the global search over people, items and claims, and the report of the whole system with the export as a text file and as CSV. Implemented in ui/search/SearchResultsPanel.java and ui/reports/ReportPanel.java. |
| Additional Screen (if any) | My Profile - the own account, the phone number, the password change and the activity of the own user. Implemented in ui/users/ProfilePanel.java. Plus the two lost-and-found screens: Post a Lost Item (title, category drop-down, brand, colour, place, description and photo) and My Posts (the own posts with the Confirm found button), both in ui/posts/. The screens of the segments 2, 3 and 4 are added into the same window with one call (MainFrame.addSegmentSection), so the whole application stays one program. |

## 8. Application of Java OOP Concepts

Explain how the following concepts will be applied in the proposed system.

| Java / OOP Concept | Planned Application in the Project |
|---|---|
| Classes and Objects | The whole domain model of core/model: User, Role, Permission, UserStatus, Session, OperationResult, DashboardStats, ItemSummary, ItemCategory, ItemType, ClaimSummary and LocationCount. Every table of the database has a class, every screen has a panel class. |
| Encapsulation | The fields of User, Session and of the records of the DAOs are private and are only reachable through getters and setters. User.sanitized() returns a copy without the password hash, so the hash cannot leak into the interface or into the audit log. |
| Inheritance | ui/common/BaseFrame is an abstract class - the base of every window of the application. LoginFrame, RegisterFrame and MainFrame extend it and inherit the title, the size, the content panel and the behaviour when the window is closed. |
| Polymorphism (overriding / overloading) | Overriding: every DAO interface has two implementations that override the same methods - JdbcUserDao for MySQL and InMemoryUserDao for the demo mode - and the application asks for the interface and never for the class. Overloading: PasswordHasher.hash(...) and ServiceRegistry.register(...) accept different parameter lists. |
| Abstraction (abstract class / interface) | The seven interfaces of core/spi (ItemCatalogService, ClaimQueryService, MatchQueryService, AdminWorkloadService, NotificationService, AuditService, SearchContributor) hide the implementation of the other segments behind a contract; the abstract class BaseFrame hides the window handling of every screen. |
| Exception Handling | try/catch inside the DAOs, the custom exceptions DataAccessException and AccessDeniedException, and OperationResult instead of exceptions across the interface: a failed login is a result with a message, not a crash of the program. The transactions are rolled back when a statement fails. |
| Collections (e.g., ArrayList / HashMap) | ArrayList and List.of() for the result lists, HashMap for the dashboard statistics and the sessions, ConcurrentHashMap for the service registry and CopyOnWriteArrayList for the listener list of the event bus. |
| Event Handling | ActionListener on the buttons, DocumentListener for the search that runs while the user is typing, FocusListener and MouseListener for the input fields and the links, WindowAdapter for the windows - and the own EventBus, which lets the segments react to each other without knowing each other. |

## 9. Database Design and JDBC Connectivity

Describe the planned database structure. Identify the main tables, their purpose, and important relationships. The project must use MySQL and JDBC for persistent data storage.

| Table Name | Purpose | Primary Key |
|---|---|---|
| users | The accounts of the students and of the staff: name, e-mail, student ID, the hashed password, the role, the status and the moment of the registration. | user_id (AUTO_INCREMENT) |
| audit_log | Every action of every user: the user, the action, the time and the details. Written by the service layer, never by the buttons. | audit_id (AUTO_INCREMENT) |
| locations | The places of the campus where an item can be lost: the name of the building and the room or the area. | location_id (AUTO_INCREMENT) |
| items | The lost and the found reports: the title, the type (LOST/FOUND), the category, the brand, the colour, the description, the status and the user who reported it. | item_id (AUTO_INCREMENT) |
| item_images | The photo of a report - the evidence that the office checks during the verification of a claim. | image_id (AUTO_INCREMENT) |
| matches | The pairs that the matching engine proposes: the lost report, the found report, the similarity score and the moment of the comparison. | match_id (AUTO_INCREMENT) |
| claims | The claims of the students: the report, the claimant, the matched pair, the status (SUBMITTED/APPROVED/REJECTED/RETURNED) and the administrator who decided. | claim_id (AUTO_INCREMENT) |
| notifications | The alerts of the dashboard: the user, the title, the text and the moment. | notification_id (AUTO_INCREMENT) |

Planned database operations (tick all that apply):

☑ INSERT     ☑ SELECT     ☑ UPDATE     ☑ DELETE     ☑ SEARCH / FILTER     ☑ JOIN / REPORT

Attach a preliminary ER diagram and relational schema. Explain how JDBC (preferably PreparedStatement) will connect the Java application to MySQL.

## 10. Tools and Technologies

| Tool / Technology | Version (if known) | Purpose |
|---|---|---|
| Java Development Kit (JDK) | Temurin 17 (LTS) | Application development - the program is compiled with plain javac, no build tool is required to run it |
| Java Swing / JavaFX | part of the JDK 17 | GUI development - the whole user interface: login, dashboard, user management, profile, audit log, search and report |
| MySQL | 8.0 | Database management - nine tables with foreign keys and indexes, created by sql/01_schema.sql; sql/02_seed_data.sql loads the three accounts and the six campus locations (no fake posts) |
| JDBC Driver | mysql-connector-j 8.4.0 | Java-database connectivity - every statement is a PreparedStatement with ? placeholders; the connections come from a pool of eight |
| IDE | IntelliJ IDEA + VS Code | Coding and debugging |
| Other tools (if any) | Git, GitHub, GitHub Actions, GitHub Pages | Version control, the pull requests, the automatic build (43 unit tests and 49 database tests on every commit) and the public project page with the screenshots |

## 11. Expected Outcomes

Describe the expected features and results of the completed application. Explain how it will address the problem and benefit its intended users.

The expected result is a working desktop application that runs on the machine of the lost and found office and stores everything in MySQL. The student registers, signs in, reports what he lost or found with a photo and follows the status of his own claim. The office sees the reports, the matching engine proposes the pairs with a similarity score, the administrator verifies the claim against the photo evidence, and the item is returned to its owner. The report of the system can be exported at any time.

The system solves the problem of the manual process in a measurable way: the register is no longer searched by eye but queried by SQL, the matching no longer depends on the memory of one person but on the score of the engine, the ownership can be proven because the photo is kept, the items that nobody claims are no longer thrown away silently because every item keeps its status until it is closed, and every decision of the office can be reviewed in the audit log. The student gets the benefit of knowing where his item is, and the office gets a system that shows the whole situation at a glance.

For the team, the project also proves that the four segments fit together: they were developed in parallel against the contracts of core/spi, integrated through the service registry, and the whole application is verified on every commit by 43 unit tests and 49 tests against a real MySQL server.

## 12. Project Timeline

| Activities | Target Completion Date |
|---|---|
| Requirement analysis and proposal approval | 12 November 2026 |
| GUI and system design (UML / ER diagram) | 26 November 2026 |
| Database creation and JDBC connection | 10 December 2026 |
| Java OOP implementation and GUI development | 24 December 2026 |
| Integration, testing, and debugging | 14 January 2027 |
| Documentation and final demonstration | 28 January 2027 |

## 13. References

List the books, official documentation, websites, tutorials, or other resources that will be used.

1. Oracle. The Java Tutorials - the sections on Creating a GUI With Swing, JDBC, Collections and Generics. https://docs.oracle.com/javase/tutorial/

2. MySQL. MySQL 8.0 Reference Manual - the chapters on the data definition language, the foreign keys and the transactions. https://dev.mysql.com/doc/refman/8.0/en/

3. Oracle. JDBC Database Access with Java - the chapters on the connection, the PreparedStatement and the result sets. https://docs.oracle.com/javase/tutorial/jdbc/

## 14. Team Member Contribution

| Student Name / ID | Assigned Tasks and Responsibilities |
|---|---|
| Member 1: MD. Nurul Azom Arif (20255103126) - Leader | Project leadership, requirement analysis and the proposal, the coordination of the four segments and of the database contract, the UML and ER diagrams, the integration of the segments into one application, the documentation of the project and the final presentation. |
| Member 2: Muntasir Al Simum (20255103134) - Co-Leader | Segment 2 - Lost and Found Item Reporting, Image Upload and Location Management: the reporting form, the image upload, the management of the campus locations, the browsing and the filtering of the open reports and the ItemCatalogService implementation. |
| Member 3: Rajmohan Chandra Shil (20255103149) - Co-Leader | Segments 3 and 4 - Intelligent Item Matching, Similarity Detection, Ownership Claim Management, Administrative Verification and Item Return Management: the matching engine with its similarity score, the claim and the verification queue, the return confirmation and the MatchQueryService, ClaimQueryService and AdminWorkloadService implementations. |
| Member 4: Sadikul Basar (20255103159) - Co-Leader | Segment 1 - User Authentication, Access Control and Dashboard Management: the whole MySQL schema and the JDBC layer with the connection pool and the transactions, the DAOs of the nine tables, the authentication with PBKDF2 hashing, the sessions, the role based access control, the audit trail, the dashboard, the global search, the report, the login, profile, user management and audit screens, and the automatic build with GitHub Actions. |

## 16. Course Teacher's Evaluation

| Proposal Decision | Course Teacher's Signature | Date |
|---|---|---|
| ☐ Approved   ☐ Needs Modification   ☐ Rejected |  |  |
