# Hospital Management System

A complete Hospital Management System built with a **Core Java API Backend** and a **Modern Web UI Frontend**. This project demonstrates full CRUD operations, appointment scheduling, and doctor management using a custom Java `HttpServer` connected to a MySQL database, consumed by a responsive HTML/CSS/JS frontend dashboard.

---

## 📋 Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Technologies Used](#technologies-used)
- [Prerequisites](#prerequisites)
- [Database Setup](#database-setup)
- [How to Run (Web Version)](#how-to-run-web-version)
- [How to Run (Legacy Console Version)](#how-to-run-legacy-console-version)

---

## ✨ Features

### Patient Management
- ✅ Add new patients (Name, Age, Gender, Contact)
- ✅ View all patients in a dynamic table
- ✅ Update existing patient details
- ✅ Delete patients from the system

### Doctor Management
- ✅ Add new doctors (Name, Specialty, Contact, Email)
- ✅ View all doctors in the directory
- ✅ Automatic dropdown population for appointments

### Appointment Management
- ✅ Book new appointments by selecting Patient and Doctor from database
- ✅ Select Date and Time for appointments
- ✅ Reschedule appointments (updates status to RESCHEDULED)
- ✅ View complete appointment history

### Billing System (Mockup)
- ✅ Select patient from database
- ✅ Generate and print dynamic billing invoices

---

## 🏗 Architecture

This project was upgraded from a CLI application to a Full-Stack Web Application:

1. **Frontend (UI Layer)**: Built with pure HTML, Vanilla CSS, and JavaScript. Uses `fetch()` API to communicate with the Java backend.
2. **Backend (API Layer)**: Built using Core Java (`com.sun.net.httpserver.HttpServer`). It handles HTTP requests, CORS policies, and JSON parsing manually.
3. **Database (Data Layer)**: MySQL Database connected via JDBC (`mysql-connector-j-9.1.0.jar`).

---

## 💻 Technologies Used

- **Frontend**: HTML5, CSS3 (Modern Cards, Flexbox, CSS Variables), JavaScript (ES6, Fetch API)
- **Backend**: Core Java (JDK 11+), `com.sun.net.httpserver.HttpServer`
- **Database**: MySQL 8.0+
- **Connectivity**: JDBC (Java Database Connectivity)

---

## 🔧 Prerequisites

1. **Java Development Kit (JDK)**: JDK 11 or higher installed and added to PATH.
2. **MySQL Server**: Installed and running locally on port `3306`.
3. **JDBC Driver**: `mysql-connector-j-9.1.0.jar` (Included in `lib` folder).
4. **Web Browser**: Chrome, Firefox, or Edge.

---

## 💾 Database Setup

1. Open your MySQL client or command line.
2. Run the provided `sql/schema.sql` script to create the database and tables:
   ```sql
   source sql/schema.sql;
   ```
3. Update database credentials if necessary inside `src/hospital/util/DatabaseConnection.java`:
   ```java
   private static final String USER = "root";
   private static final String PASS = "ashish123"; // Update with your MySQL password
   ```

---

## 🚀 How to Run (Web Version)

Follow these steps to run the complete Full-Stack web application.

### Step 1: Start the Java API Server
Open your terminal at the root directory of the project (`Hospital_management_system`) and compile the files:

```powershell
javac -cp "lib/mysql-connector-j-9.1.0.jar" -d out src/hospital/util/*.java src/hospital/server/ApiServer.java
```

Run the API Server:

```powershell
java -cp "out;lib/mysql-connector-j-9.1.0.jar" hospital.server.ApiServer
```
*You should see: `API Server is starting on http://localhost:8080`*

### Step 2: Open the Frontend Dashboard
Simply double-click the `frontend/index.html` file to open it in your browser, or open it using VS Code Live Server.
- The dashboard will automatically fetch data from `http://localhost:8080`.
- Ensure the Java Server remains running in the terminal while you use the web application!

---

## 🖥 How to Run (Legacy Console Version)

If you prefer to run the older Command-Line Interface version of the app:

```powershell
javac -cp "lib/mysql-connector-j-9.1.0.jar" -d out src/hospital/**/*.java
java -cp "out;lib/mysql-connector-j-9.1.0.jar" hospital.HospitalManagementSystem
```

---
*Developed as a Core Java Full-Stack Integration Project.*