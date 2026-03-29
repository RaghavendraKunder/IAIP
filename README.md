# Online Examination System (IAIP)

A full-stack online exam platform with Java servlet backend (Tomcat + JDBC + MySQL) and modern frontend (React + Vite + Tailwind). It includes:
- Admin/college registration and login
- Student registration, login and exam participation
- Exam creation, question management, publish/unpublish
- Real-time scoring and results

## Repository layout

- `backend/` : Java servlet project (Eclipse-style project with `src/main/java`, `WebContent`, `WEB-INF`, etc.)
- `frontend/` : Vite React + TypeScript UI + static admin/student pages

## Tech stack

- Backend: Java 8+ (or 11+), Servlet API, JDBC, MySQL
- Frontend: React 18, TypeScript, Vite, Tailwind CSS, React Router
- Database: MySQL (default DB name `examflow`)

## Prerequisites

1. Java JDK (11+ recommended)
2. Apache Tomcat 9/10
3. MySQL 5.7+ / 8 (or MariaDB)
4. Node.js 18+ and npm / bun
5. (Optional) IDE: Eclipse, IntelliJ, VS Code

## Backend setup (Java + Tomcat)

### 1. Import project

- Open Eclipse (or IntelliJ/VS Code with Java web plugins)
- Import -> Existing Projects into Workspace -> select `backend` folder
- Ensure `WebContent` is recognized and `WEB-INF/web.xml` exists.

### 2. Add JDBC connector

- Copy `mysql-connector-j-9.6.0.jar` into `backend/WebContent/WEB-INF/lib/`
- (`backend/libs.md` mentions this dependency)

### 3. Configure DB connection

- File: `backend/src/main/java/util/DBConnection.java`
- Default values:
  - URL: `jdbc:mysql://localhost:3306/examflow?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true`
  - USER: `root`
  - PASSWORD: `Sujat@1972` (change before production)

### 4. Configure Tomcat deployment

- Add Tomcat server in Eclipse
- Add project to the server
- Start server

### 5. Run backend

- Access servlet endpoint: `http://localhost:8085/Online_Examination_System/...` (based on `BASE` set in frontend files)

## Database setup (MySQL)

Execute these SQL commands in MySQL (MySQL Workbench / CLI):

```sql
CREATE DATABASE IF NOT EXISTS examflow DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE examflow;

CREATE TABLE colleges (
    id INT AUTO_INCREMENT PRIMARY KEY,
    college_name VARCHAR(255) NOT NULL UNIQUE,
    admin_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    admin_key_hash VARCHAR(255) NOT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE students (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    roll_no VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    college_name VARCHAR(255) NOT NULL,
    college_id INT NULL,
    class_level VARCHAR(50) NOT NULL,
    stream VARCHAR(100),
    course VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX (college_id),
    INDEX (college_name)
);

CREATE TABLE exams (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    duration_minutes INT NOT NULL,
    total_marks INT NOT NULL,
    deadline VARCHAR(64),
    is_published TINYINT(1) NOT NULL DEFAULT 0,
    college_name VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE questions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    exam_id INT NOT NULL,
    question_text TEXT NOT NULL,
    option_a TEXT NOT NULL,
    option_b TEXT NOT NULL,
    option_c TEXT NOT NULL,
    option_d TEXT NOT NULL,
    correct_option VARCHAR(16) NOT NULL,
    marks INT NOT NULL,
    FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE
);

CREATE TABLE results (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    exam_id INT NOT NULL,
    score INT NOT NULL,
    total_marks INT NOT NULL,
    attempted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id),
    FOREIGN KEY (exam_id) REFERENCES exams(id)
);
```

### Optional data

- Create an admin college manually by entering hashed passwords in `colleges`.
- Student registration / college registration flows in app manage data automatically.

## Frontend setup (React + Vite)

### 1. Install dependencies

From `frontend` folder:

```bash
cd frontend
npm install
# or bun install if using bun (repo indicates bun.lockb)
# bun install
```

### 2. Run dev server

```bash
npm run dev
```

- Open: `http://localhost:5173`

### 3. Build for production

```bash
npm run build
npm run preview
```

## How to test flows

1. Start DB and run SQL schema
2. Start backend Tomcat (project deployed)
3. Start frontend dev
4. Open app landing page and use college/student flows
5. Admin actions: new exam, add questions, publish
6. Student actions: view exam list, start exam, submit result

## Routing and API

- Backend path root: `http://localhost:8085/Online_Examination_System`
- Frontend default API base in static pages and React: `BASE = "http://localhost:8085/Online_Examination_System"`
- Key endpoints (examples):
  - `POST /api/college/register`
  - `POST /api/register`
  - `POST /api/login`
  - `GET /student/exams?studentId=X`
  - `POST /exam/submit`
  - `GET /admin/students` etc.

## Notes

- `DBConnection` has hard-coded credentials; move to environment files for production.
- Ensure same DB name and credentials across backend and MySQL setup.
- If port mismatch or context path changes, update `BASE` in frontend files and `web.xml` if required.

## Troubleshooting

- `ClassNotFoundException: com.mysql.cj.jdbc.Driver`: Add `mysql-connector` jar to `WEB-INF/lib`.
- `Access denied for user 'root'`: check MySQL root password or set up a dedicated user.
- CORS issue using separate frontend host: add CORS filter to servlet or use proxy.

## Useful files

- `backend/src/main/java/util/DBConnection.java` (JDBC URL, user, password)
- `backend/src/main/java/dao/*.java` (table/column mapping)
- `frontend/public/*.html`, `frontend/src/components/landing/AuthModal.tsx` (login/register UI)

---

Happy coding! 👨‍💻
