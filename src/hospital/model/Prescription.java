package hospital.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * Prescription - Model class for Prescription entity
 * Handles all prescription-related database operations
 */
public class Prescription {
    
    private Connection connection;
    private Scanner scanner;
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // Constructor
    public Prescription(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }
    
    /**
     * Add a new prescription
     */
    public void addPrescription() {
        try {
            System.out.print("Enter patient ID: ");
            while (!scanner.hasNextInt()) {
                System.out.print("Invalid input. Please enter a valid patient ID: ");
                scanner.next();
            }
            int patientId = scanner.nextInt();
            
            System.out.print("Enter doctor ID: ");
            while (!scanner.hasNextInt()) {
                System.out.print("Invalid input. Please enter a valid doctor ID: ");
                scanner.next();
            }
            int doctorId = scanner.nextInt();
            
            // Validate patient and doctor
            if (!isValidPatient(patientId)) {
                System.out.println("\n✗ Invalid patient ID!");
                return;
            }
            
            if (!isValidDoctor(doctorId)) {
                System.out.println("\n✗ Invalid doctor ID!");
                return;
            }
            
            // Optional: Link to existing appointment
            System.out.print("Enter appointment ID (or 0 if no appointment): ");
            while (!scanner.hasNextInt()) {
                System.out.print("Invalid input. Please enter a valid appointment ID: ");
                scanner.next();
            }
            int appointmentId = scanner.nextInt();
            if (appointmentId == 0) {
                appointmentId = -1; // Will be set to NULL in database
            }
            
            scanner.nextLine(); // Consume newline
            System.out.print("Enter diagnosis: ");
            String diagnosis = scanner.nextLine();
            
            System.out.print("Enter medicines (comma separated): ");
            String medicines = scanner.nextLine();
            
            System.out.print("Enter notes: ");
            String notes = scanner.nextLine();
            
            String prescriptionDate = LocalDate.now().format(DATE_FORMAT);
            
            String query = "INSERT INTO prescriptions (patient_id, doctor_id, appointment_id, diagnosis, medicines, notes, prescription_date) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, patientId);
                preparedStatement.setInt(2, doctorId);
                preparedStatement.setObject(3, appointmentId > 0 ? appointmentId : null);
                preparedStatement.setString(4, diagnosis);
                preparedStatement.setString(5, medicines);
                preparedStatement.setString(6, notes);
                preparedStatement.setString(7, prescriptionDate);
                
                int affectedRows = preparedStatement.executeUpdate();
                if (affectedRows > 0) {
                    System.out.println("\n✓ Prescription added successfully!");
                } else {
                    System.out.println("\n✗ Failed to add prescription!");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding prescription: " + e.getMessage());
        }
    }
    
    /**
     * View all prescriptions
     */
    public void viewPrescriptions() {
        String query = "SELECT pr.id, p.name as patient_name, d.name as doctor_name, " +
                      "pr.diagnosis, pr.medicines, pr.notes, pr.prescription_date, pr.status " +
                      "FROM prescriptions pr " +
                      "JOIN patients p ON pr.patient_id = p.id " +
                      "JOIN doctors d ON pr.doctor_id = d.id " +
                      "ORDER BY pr.prescription_date DESC";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            
            System.out.println("\n" + "=".repeat(120));
            System.out.printf("%-5s %-20s %-25s %-20s %-30s %-12s\n", 
                "ID", "Patient", "Doctor", "Diagnosis", "Medicines", "Date");
            System.out.println("=".repeat(120));
            
            boolean hasData = false;
            while (resultSet.next()) {
                hasData = true;
                String diagnosis = resultSet.getString("diagnosis");
                String medicines = resultSet.getString("medicines");
                
                // Truncate long strings for display
                if (diagnosis != null && diagnosis.length() > 20) {
                    diagnosis = diagnosis.substring(0, 17) + "...";
                }
                if (medicines != null && medicines.length() > 30) {
                    medicines = medicines.substring(0, 27) + "...";
                }
                
                System.out.printf("%-5d %-20s %-25s %-20s %-30s %-12s\n",
                    resultSet.getInt("id"),
                    resultSet.getString("patient_name"),
                    resultSet.getString("doctor_name"),
                    diagnosis,
                    medicines,
                    resultSet.getDate("prescription_date"));
            }
            
            if (!hasData) {
                System.out.println("No prescriptions found!");
            }
            System.out.println("=".repeat(120));
            
        } catch (SQLException e) {
            System.err.println("Error viewing prescriptions: " + e.getMessage());
        }
    }
    
    /**
     * View prescription history for a specific patient
     */
    public void viewPrescriptionHistory() {
        System.out.print("Enter patient ID: ");
        while (!scanner.hasNextInt()) {
            System.out.print("Invalid input. Please enter a valid patient ID: ");
            scanner.next();
        }
        int patientId = scanner.nextInt();
        
        // Validate patient
        if (!isValidPatient(patientId)) {
            System.out.println("\n✗ Invalid patient ID!");
            return;
        }
        
        String query = "SELECT pr.id, d.name as doctor_name, pr.diagnosis, pr.medicines, " +
                      "pr.notes, pr.prescription_date " +
                      "FROM prescriptions pr " +
                      "JOIN doctors d ON pr.doctor_id = d.id " +
                      "WHERE pr.patient_id = ? " +
                      "ORDER BY pr.prescription_date DESC";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, patientId);
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                System.out.println("\n" + "=".repeat(100));
                System.out.println("Prescription History for Patient ID: " + patientId);
                System.out.println("=".repeat(100));
                
                boolean hasData = false;
                while (resultSet.next()) {
                    hasData = true;
                    System.out.println("\n--- Prescription ID: " + resultSet.getInt("id") + " ---");
                    System.out.println("Doctor:     " + resultSet.getString("doctor_name"));
                    System.out.println("Date:       " + resultSet.getDate("prescription_date"));
                    System.out.println("Diagnosis:  " + resultSet.getString("diagnosis"));
                    System.out.println("Medicines:  " + resultSet.getString("medicines"));
                    System.out.println("Notes:      " + resultSet.getString("notes"));
                    System.out.println("-".repeat(50));
                }
                
                if (!hasData) {
                    System.out.println("No prescription history found for this patient!");
                }
                System.out.println("=".repeat(100));
            }
        } catch (SQLException e) {
            System.err.println("Error viewing prescription history: " + e.getMessage());
        }
    }
    
    /**
     * View prescription details by ID
     */
    public void viewPrescriptionById() {
        System.out.print("Enter prescription ID: ");
        while (!scanner.hasNextInt()) {
            System.out.print("Invalid input. Please enter a valid prescription ID: ");
            scanner.next();
        }
        int prescriptionId = scanner.nextInt();
        
        String query = "SELECT pr.id, p.name as patient_name, p.id as patient_id, " +
                      "d.name as doctor_name, d.id as doctor_id, " +
                      "pr.diagnosis, pr.medicines, pr.notes, pr.prescription_date " +
                      "FROM prescriptions pr " +
                      "JOIN patients p ON pr.patient_id = p.id " +
                      "JOIN doctors d ON pr.doctor_id = d.id " +
                      "WHERE pr.id = ?";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, prescriptionId);
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    System.out.println("\n" + "=".repeat(80));
                    System.out.println("Prescription Details:");
                    System.out.println("=".repeat(80));
                    System.out.println("Prescription ID:  " + resultSet.getInt("id"));
                    System.out.println("Patient ID:       " + resultSet.getInt("patient_id"));
                    System.out.println("Patient Name:     " + resultSet.getString("patient_name"));
                    System.out.println("Doctor ID:        " + resultSet.getInt("doctor_id"));
                    System.out.println("Doctor Name:      " + resultSet.getString("doctor_name"));
                    System.out.println("Date:             " + resultSet.getDate("prescription_date"));
                    System.out.println("Diagnosis:        " + resultSet.getString("diagnosis"));
                    System.out.println("Medicines:        " + resultSet.getString("medicines"));
                    System.out.println("Notes:            " + resultSet.getString("notes"));
                    System.out.println("=".repeat(80));
                } else {
                    System.out.println("\n✗ Prescription with ID " + prescriptionId + " not found!");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error viewing prescription: " + e.getMessage());
        }
    }
    
    /**
     * Validate patient exists
     */
    private boolean isValidPatient(int patientId) {
        String query = "SELECT id FROM patients WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Validate doctor exists
     */
    private boolean isValidDoctor(int doctorId) {
        String query = "SELECT id FROM doctors WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }
}