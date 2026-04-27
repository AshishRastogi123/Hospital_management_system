-- =====================================================
-- HOSPITAL MANAGEMENT SYSTEM - SCHEMA MIGRATION SQL
-- =====================================================
-- This file contains SQL queries to update existing database schema
-- Run these queries if you already have the old schema and want to add new columns
-- =====================================================

-- =====================================================
-- STEP 1: Update PATIENTS table
-- =====================================================

-- Add new columns to patients table (run only if columns don't exist)
ALTER TABLE patients ADD COLUMN IF NOT EXISTS phone VARCHAR(20);
ALTER TABLE patients ADD COLUMN IF NOT EXISTS email VARCHAR(100);
ALTER TABLE patients ADD COLUMN IF NOT EXISTS address TEXT;
ALTER TABLE patients ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE patients ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- =====================================================
-- STEP 2: Update DOCTORS table
-- =====================================================

-- Add new columns to doctors table (run only if columns don't exist)
ALTER TABLE doctors ADD COLUMN IF NOT EXISTS phone VARCHAR(20);
ALTER TABLE doctors ADD COLUMN IF NOT EXISTS email VARCHAR(100);
ALTER TABLE doctors ADD COLUMN IF NOT EXISTS experience_years INT;
ALTER TABLE doctors ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE doctors ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- =====================================================
-- STEP 3: Update APPOINTMENTS table
-- =====================================================

-- Add appointment_time column (IMPORTANT for time slot feature)
ALTER TABLE appointments ADD COLUMN IF NOT EXISTS appointment_time TIME NOT NULL;

-- Add status column with default value
ALTER TABLE appointments ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'SCHEDULED';

-- Add timestamp columns
ALTER TABLE appointments ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE appointments ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- =====================================================
-- STEP 4: Create PRESCRIPTIONS table
-- =====================================================

-- Create prescriptions table if it doesn't exist
CREATE TABLE IF NOT EXISTS prescriptions (
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
-- STEP 5: Add unique constraint to prevent double booking
-- =====================================================

-- Add unique constraint for doctor + date + time combination
-- Note: This may fail if there are existing duplicate appointments
ALTER TABLE appointments ADD UNIQUE INDEX IF NOT EXISTS unique_appointment (doctor_id, appointment_date, appointment_time);

-- =====================================================
-- STEP 6: Update existing appointments with time
-- =====================================================

-- If appointment_time was added as NOT NULL, set default time for existing records
UPDATE appointments SET appointment_time = '10:00:00' WHERE appointment_time IS NULL;

-- =====================================================
-- VERIFICATION QUERIES
-- =====================================================

-- Check patients table structure
-- DESCRIBE patients;

-- Check doctors table structure
-- DESCRIBE doctors;

-- Check appointments table structure
-- DESCRIBE appointments;

-- Check prescriptions table structure
-- DESCRIBE prescriptions;

-- =====================================================
-- SAMPLE DATA (Optional - for testing)
-- =====================================================

-- Insert sample doctors if doctors table is empty
-- INSERT INTO doctors (name, specialty, phone, email, experience_years) VALUES
-- ('Dr. Sarah Johnson', 'Cardiology', '555-0101', 'sarah.johnson@hospital.com', 15),
-- ('Dr. Michael Chen', 'Neurology', '555-0102', 'michael.chen@hospital.com', 12),
-- ('Dr. Emily Williams', 'Pediatrics', '555-0103', 'emily.williams@hospital.com', 10);

-- =====================================================
-- ROLLBACK (If needed)
-- =====================================================

-- To remove new columns (use with caution):
-- ALTER TABLE patients DROP COLUMN IF EXISTS phone;
-- ALTER TABLE patients DROP COLUMN IF EXISTS email;
-- ALTER TABLE patients DROP COLUMN IF EXISTS address;
-- ALTER TABLE doctors DROP COLUMN IF EXISTS phone;
-- ALTER TABLE doctors DROP COLUMN IF EXISTS email;
-- ALTER TABLE doctors DROP COLUMN IF EXISTS experience_years;
-- ALTER TABLE appointments DROP COLUMN IF EXISTS appointment_time;
-- ALTER TABLE appointments DROP COLUMN IF EXISTS status;
-- DROP TABLE IF EXISTS prescriptions;