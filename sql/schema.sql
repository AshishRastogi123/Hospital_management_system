-- =====================================================
-- HOSPITAL MANAGEMENT SYSTEM - DATABASE SCHEMA
-- =====================================================

-- Drop existing tables if they exist (in correct order due to foreign keys)
DROP TABLE IF EXISTS prescriptions;
DROP TABLE IF EXISTS appointments;
DROP TABLE IF EXISTS doctors;
DROP TABLE IF EXISTS patients;

-- =====================================================
-- PATIENTS TABLE
-- =====================================================
CREATE TABLE patients (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    age INT NOT NULL,
    gender VARCHAR(10) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- =====================================================
-- DOCTORS TABLE
-- =====================================================
CREATE TABLE doctors (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    specialty VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    experience_years INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- =====================================================
-- APPOINTMENTS TABLE (With Time Slot Support)
-- =====================================================
CREATE TABLE appointments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    status VARCHAR(20) DEFAULT 'SCHEDULED',
    -- Status can be: SCHEDULED, COMPLETED, CANCELLED, NO_SHOW
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE,
    -- Unique constraint to prevent double booking
    UNIQUE KEY unique_appointment (doctor_id, appointment_date, appointment_time)
);

-- =====================================================
-- PRESCRIPTIONS TABLE
-- =====================================================
CREATE TABLE prescriptions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    appointment_id INT,
    diagnosis TEXT NOT NULL,
    medicines TEXT NOT NULL,
    notes TEXT,
    prescription_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE,
    FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE SET NULL
);

-- =====================================================
-- SAMPLE DATA - DOCTORS
-- =====================================================
INSERT INTO doctors (name, specialty, phone, email, experience_years) VALUES
('Dr. Sarah Johnson', 'Cardiology', '555-0101', 'sarah.johnson@hospital.com', 15),
('Dr. Michael Chen', 'Neurology', '555-0102', 'michael.chen@hospital.com', 12),
('Dr. Emily Williams', 'Pediatrics', '555-0103', 'emily.williams@hospital.com', 10),
('Dr. James Brown', 'Orthopedics', '555-0104', 'james.brown@hospital.com', 18),
('Dr. Lisa Anderson', 'Dermatology', '555-0105', 'lisa.anderson@hospital.com', 8);

-- =====================================================
-- SAMPLE DATA - PATIENTS
-- =====================================================
INSERT INTO patients (name, age, gender, phone, email, address) VALUES
('John Smith', 35, 'Male', '555-1001', 'john.smith@email.com', '123 Main St'),
('Mary Davis', 28, 'Female', '555-1002', 'mary.davis@email.com', '456 Oak Ave'),
('Robert Wilson', 45, 'Male', '555-1003', 'robert.wilson@email.com', '789 Pine Rd');

-- =====================================================
-- SAMPLE DATA - APPOINTMENTS
-- =====================================================
INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, status) VALUES
(1, 1, '2026-04-28', '10:00:00', 'SCHEDULED'),
(2, 2, '2026-04-28', '11:30:00', 'SCHEDULED'),
(3, 3, '2026-04-29', '09:00:00', 'COMPLETED');

-- =====================================================
-- SAMPLE DATA - PRESCRIPTIONS
-- =====================================================
INSERT INTO prescriptions (patient_id, doctor_id, appointment_id, diagnosis, medicines, notes, prescription_date) VALUES
(3, 3, 3, 'Common Cold', 'Paracetamol 500mg, Vitamin C 1000mg', 'Rest and stay hydrated', '2026-04-29');