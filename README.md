# Hospital Management System

A production-style Java + JDBC + MySQL console application for managing hospital operations. This project demonstrates full CRUD operations, appointment scheduling with time slots, prescription management, and proper database design with foreign key relationships.

---

## 📋 Table of Contents

- [Features](#features)
- [Project Structure](#project-structure)
- [Technologies Used](#technologies-used)
- [Prerequisites](#prerequisites)
- [Database Setup](#database-setup)
- [How to Run](#how-to-run)
- [Menu Options](#menu-options)
- [Usage Examples](#usage-examples)
- [Project Design](#project-design)
- [Troubleshooting](#troubleshooting)

---

## ✨ Features

### Patient Management
- ✅ Add new patients (name, age, gender, phone, email, address)
- ✅ View all patients
- ✅ Search patients by ID or Name
- ✅ Update patient details
- ✅ Delete patients

### Doctor Management
- ✅ View all doctors
- ✅ View doctor by ID
- ✅ View doctors by specialty

### Appointment Management
- ✅ Book appointments with time slots (30-minute intervals)
- ✅ View all appointments
- ✅ Cancel appointments
- ✅ Reschedule appointments
- ✅ Check doctor availability
- ✅ **Double-booking prevention** - prevents same doctor booking at same time
- ✅ **Patient duplicate prevention** - prevents same patient booking same slot

### Prescription Management
- ✅ Add prescriptions (diagnosis, medicines, notes)
- ✅ View all prescriptions
- ✅ View prescription history for a patient

### Technical Features
- ✅ Object-oriented design with proper package structure
- ✅ Singleton pattern for database connection
- ✅ PreparedStatement for all SQL queries (SQL injection prevention)
- ✅ Input validation and exception handling
- ✅ Clean separation of concerns (Model-View-Controller pattern)

---

## 📂 Project Structure

```
hospital-management/
├── lib/
│   └── mysql-connector-j-9.1.0.jar    # MySQL JDBC Driver
├── sql/
│   ├── schema.sql                      # Complete database schema
│   ├── schema_migration.sql            # Migration queries
│   └── schema_update_queries.txt       # Plain text SQL queries
├── src/
│   └── hospital/
│       ├── util/
│       │   └── DatabaseConnection.java # Singleton DB connection
│       ├── model/
│       │   ├── Patient.java           # Patient CRUD operations
│       │   ├── Doctor.java            # Doctor operations
│       │   ├── Appointment.java       # Appointment with time slots
│       │   └── Prescription.java      # Prescription management
│       └── HospitalManagementSystem.java # Main application
├── out/                                # Compiled classes
├── README.md
└── .gitignore
```

---

## 🛠 Technologies Used

| Technology | Description |
|------------|-------------|
| **Java 17+** | Programming language |
| **MySQL 8.0+** | Database |
| **JDBC** | Java Database Connectivity |
| **MySQL Connector/J** | JDBC driver for MySQL |
| **VS Code** | Recommended IDE |

---

## 📌 Prerequisites

Before running the project, ensure you have:

1. **Java Development Kit (JDK) 17 or higher**
   ```powershell
   java -version
   ```

2. **MySQL Server 8.0 or higher**
   - MySQL should be running on `localhost:3306`
   - Default credentials: `root` / `your_password` (update in code if different)

3. **MySQL Connector/J JAR file**
   - Already included in `lib/` folder

---

## 🗄 Database Setup

### Option 1: Fresh Database (Recommended)

Run the schema file to create a new database with sample data:

```powershell
mysql -u root -<your_password> < sql/schema.sql
```

Or in MySQL Workbench/MySQL Shell:
```sql
SOURCE path/to/hospital-management/sql/schema.sql;
```

### Option 2: Update Existing Database

If you already have the old database schema, run the migration queries:

```powershell
mysql -u root -<your_password> hospital < sql/schema_migration.sql
```

Or use the text file with plain SQL queries:
```powershell
mysql -u root -<your_password> hospital < sql/schema_update_queries.txt
```

### Database Credentials

If you need to change the database credentials, edit this file:
- **File**: `src/hospital/util/DatabaseConnection.java`
- **Lines to modify**:
  ```java
  private static final String URL = "jdbc:mysql://localhost:3306/hospital";
  private static final String USERNAME = "root";
  private static final String PASSWORD = <your_password>";
  ```

---

## 🚀 How to Run

### Step 1: Navigate to Project Directory

```powershell
cd D:\java_project\hospital-management
```

### Step 2: Compile the Project

```powershell
javac -cp "lib/mysql-connector-j-9.1.0.jar" -d out src/hospital/util/*.java src/hospital/model/*.java src/hospital/*.java
```

### Step 3: Run the Application

```powershell
java -cp "out;lib/mysql-connector-j-9.1.0.jar" hospital.HospitalManagementSystem
```

> **Note**: On Windows, use `;` as classpath separator. On Linux/Mac, use `:` instead.

---

## 📱 Menu Options

```
============================================================
           HOSPITAL MANAGEMENT SYSTEM
============================================================
  1.  Add Patient
  2.  View Patients
  3.  Search Patient
  4.  Update Patient
  5.  Delete Patient
  6.  View Doctors
  7.  Book Appointment
  8.  View Appointments
  9.  Cancel Appointment
 10. Reschedule Appointment
 11. Check Doctor Availability
 12. Add Prescription
 13. View Prescriptions
 14. Exit
============================================================
```

---

## 💡 Usage Examples

### 1. Adding a Patient

```
Enter your choice: 1

Enter patient name: John Doe
Enter patient age: 35
Enter patient gender (Male/Female/Other): Male
Enter phone number: 555-1234
Enter email: john.doe@email.com
Enter address: 123 Main Street, City

✓ Patient added successfully!
```

### 2. Booking an Appointment with Time Slot

```
Enter your choice: 7

Enter patient ID: 1
Enter doctor ID: 1
Enter appointment date (YYYY-MM-DD): 2026-04-28

--- Available Time Slots for 2026-04-28 ---

Slotted Time        Status
----------------------------------------
 1. [09:00 AM] - AVAILABLE
 2. [09:30 AM] - BOOKED
 3. [10:00 AM] - AVAILABLE
 4. [10:30 AM] - AVAILABLE
...

Select time slot number: 1

✓ Appointment booked successfully!
  Date: 2026-04-28 Time: 09:00:00
```

### 3. Double Booking Prevention

```
Enter your choice: 7

Enter patient ID: 1
Enter doctor ID: 1
Enter appointment date (YYYY-MM-DD): 2026-04-28
Select time slot number: 2

✗ This time slot is already booked!
Please choose another slot.
```

### 4. Adding a Prescription

```
Enter your choice: 12

Enter patient ID: 1
Enter doctor ID: 1
Enter appointment ID (or 0 if no appointment): 0
Enter diagnosis: Common Cold
Enter medicines (comma separated): Paracetamol 500mg, Vitamin C 1000mg
Enter notes: Rest and stay hydrated

✓ Prescription added successfully!
```

---

## 🏗 Project Design

### Package Structure

```
hospital/
├── util/           # Utility classes
│   └── DatabaseConnection.java    # Singleton connection manager
├── model/          # Data models (business logic)
│   ├── Patient.java
│   ├── Doctor.java
│   ├── Appointment.java
│   └── Prescription.java
└── HospitalManagementSystem.java  # Main controller
```

### Design Patterns Used

| Pattern | Implementation |
|---------|----------------|
| **Singleton** | DatabaseConnection class |
| **DAO** | Model classes handle all DB operations |
| **Factory** | Implicit via constructors |
| **Encapsulation** | Private fields with public getters/setters |

### Database Schema

```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│  patients   │       │   doctors   │       │appointments │
├─────────────┤       ├─────────────┤       ├─────────────┤
│ id (PK)     │◄─────►│ id (PK)     │◄─────►│ patient_id  │
│ name        │       │ name        │       │ doctor_id   │
│ age         │       │ specialty   │       │ date        │
│ gender      │       │ phone       │       │ time        │
│ phone       │       │ email       │       │ status      │
│ email       │       │ experience  │       └─────────────┘
│ address     │       └─────────────┘              │
└─────────────┘                                     ▼
                                              ┌─────────────┐
                                              │prescriptions│
                                              ├─────────────┤
                                              │ patient_id  │
                                              │ doctor_id   │
                                              │ appointment │
                                              │ diagnosis   │
                                              │ medicines   │
                                              │ notes       │
                                              └─────────────┘
```

---

## 🔧 Troubleshooting

### Common Issues

#### 1. "No suitable driver found"

```
java.sql.SQLException: No suitable driver found
```

**Solution**: Make sure the MySQL Connector JAR is in the classpath:
```powershell
java -cp "out;lib/mysql-connector-j-9.1.0.jar" hospital.HospitalManagementSystem
```

#### 2. "Access denied for user"

```
java.sql.SQLException: Access denied for user 'root'@'localhost'
```

**Solution**: Check your MySQL username and password in `DatabaseConnection.java`

#### 3. "Unknown database 'hospital'"

```
java.sql.SQLException: Unknown database 'hospital'
```

**Solution**: Run the schema file to create the database:
```powershell
mysql -u root -<your_password> < sql/schema.sql
```

#### 4. "Column not found" errors

**Solution**: Your database schema is outdated. Run migration:
```powershell
mysql -u root -<your_password> hospital < sql/schema_migration.sql
```

#### 5. Compilation errors

**Solution**: Clean and recompile:
```powershell
rm -rf out
mkdir out
javac -cp "lib/mysql-connector-j-9.1.0.jar" -d out src/hospital/util/*.java src/hospital/model/*.java src/hospital/*.java
```

---

## 📝 Sample Data

The schema includes sample data for testing:

### Doctors
| ID | Name | Specialty |
|----|------|-----------|
| 1 | Dr. Sarah Johnson | Cardiology |
| 2 | Dr. Michael Chen | Neurology |
| 3 | Dr. Emily Williams | Pediatrics |
| 4 | Dr. James Brown | Orthopedics |
| 5 | Dr. Lisa Anderson | Dermatology |

### Patients
| ID | Name | Age | Gender |
|----|------|-----|--------|
| 1 | John Smith | 35 | Male |
| 2 | Mary Davis | 28 | Female |
| 3 | Robert Wilson | 45 | Male |

---

## 🎯 Learning Outcomes

After studying this project, you will understand:

1. **JDBC Connection** - How to connect Java with MySQL using JDBC
2. **CRUD Operations** - Perform Create, Read, Update, Delete operations
3. **PreparedStatement** - Prevent SQL injection, handle dynamic queries
4. **Object-Oriented Design** - Proper class structure and encapsulation
5. **Database Design** - Foreign keys, relationships, schema design
6. **Error Handling** - Graceful exception handling in Java

---

## 📄 License

This project is for educational purposes.

---

## 👤 Author

Created as a portfolio project for Java + JDBC learning.

---

## 🔗 Quick Reference

| Command | Description |
|---------|-------------|
| Compile | `javac -cp "lib/mysql-connector-j-9.1.0.jar" -d out src/hospital/**/*.java src/hospital/*.java` |
| Run | `java -cp "out;lib/mysql-connector-j-9.1.0.jar" hospital.HospitalManagementSystem` |
| Create DB | `mysql -u root -p<your_password> < sql/schema.sql` |

---