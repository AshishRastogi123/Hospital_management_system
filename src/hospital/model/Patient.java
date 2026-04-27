package hospital.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Patient - Model class for Patient entity
 * Handles all patient-related database operations
 */
public class Patient {
    
    private Connection connection;
    private Scanner scanner;
    
    // Constructor
    public Patient(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }
    
    /**
     * Add a new patient to the database
     */
    public void addPatient() {
        try {
            scanner.nextLine(); // Consume newline
            
            System.out.print("Enter patient name: ");
            String name = scanner.nextLine();
            
            System.out.print("Enter patient age: ");
            while (!scanner.hasNextInt()) {
                System.out.print("Invalid input. Please enter a valid age (integer): ");
                scanner.next();
            }
            int age = scanner.nextInt();
            
            System.out.print("Enter patient gender (Male/Female/Other): ");
            String gender = scanner.next();
            
            System.out.print("Enter phone number: ");
            String phone = scanner.next();
            
            System.out.print("Enter email: ");
            String email = scanner.next();
            
            scanner.nextLine(); // Consume newline
            System.out.print("Enter address: ");
            String address = scanner.nextLine();
            
            String query = "INSERT INTO patients (name, age, gender, phone, email, address) VALUES (?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, name);
                preparedStatement.setInt(2, age);
                preparedStatement.setString(3, gender);
                preparedStatement.setString(4, phone);
                preparedStatement.setString(5, email);
                preparedStatement.setString(6, address);
                
                int affectedRows = preparedStatement.executeUpdate();
                if (affectedRows > 0) {
                    System.out.println("\n✓ Patient added successfully!");
                } else {
                    System.out.println("\n✗ Failed to add patient!");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding patient: " + e.getMessage());
        }
    }
    
    /**
     * View all patients from the database
     */
    public void viewPatients() {
        String query = "SELECT * FROM patients ORDER BY id";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            
            System.out.println("\n" + "=".repeat(100));
            System.out.printf("%-5s %-20s %-5s %-10s %-15s %-25s %s\n", 
                "ID", "Name", "Age", "Gender", "Phone", "Email", "Address");
            System.out.println("=".repeat(100));
            
            boolean hasData = false;
            while (resultSet.next()) {
                hasData = true;
                System.out.printf("%-5d %-20s %-5d %-10s %-15s %-25s %s\n",
                    resultSet.getInt("id"),
                    resultSet.getString("name"),
                    resultSet.getInt("age"),
                    resultSet.getString("gender"),
                    resultSet.getString("phone"),
                    resultSet.getString("email"),
                    resultSet.getString("address"));
            }
            
            if (!hasData) {
                System.out.println("No patients found!");
            }
            System.out.println("=".repeat(100));
            
        } catch (SQLException e) {
            System.err.println("Error viewing patients: " + e.getMessage());
        }
    }
    
    /**
     * Search patient by ID
     */
    public void searchPatientById() {
        System.out.print("Enter patient ID to search: ");
        while (!scanner.hasNextInt()) {
            System.out.print("Invalid input. Please enter a valid patient ID: ");
            scanner.next();
        }
        int patientId = scanner.nextInt();
        
        String query = "SELECT * FROM patients WHERE id = ?";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, patientId);
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    System.out.println("\n" + "=".repeat(80));
                    System.out.println("Patient Details:");
                    System.out.println("=".repeat(80));
                    System.out.println("ID:       " + resultSet.getInt("id"));
                    System.out.println("Name:     " + resultSet.getString("name"));
                    System.out.println("Age:      " + resultSet.getInt("age"));
                    System.out.println("Gender:  " + resultSet.getString("gender"));
                    System.out.println("Phone:    " + resultSet.getString("phone"));
                    System.out.println("Email:    " + resultSet.getString("email"));
                    System.out.println("Address:  " + resultSet.getString("address"));
                    System.out.println("=".repeat(80));
                } else {
                    System.out.println("\n✗ Patient with ID " + patientId + " not found!");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching patient: " + e.getMessage());
        }
    }
    
    /**
     * Search patient by Name
     */
    public void searchPatientByName() {
        scanner.nextLine(); // Consume newline
        System.out.print("Enter patient name to search: ");
        String name = scanner.nextLine();
        
        String query = "SELECT * FROM patients WHERE name LIKE ?";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, "%" + name + "%");
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                System.out.println("\n" + "=".repeat(100));
                System.out.printf("%-5s %-20s %-5s %-10s %-15s %-25s %s\n", 
                    "ID", "Name", "Age", "Gender", "Phone", "Email", "Address");
                System.out.println("=".repeat(100));
                
                boolean hasData = false;
                while (resultSet.next()) {
                    hasData = true;
                    System.out.printf("%-5d %-20s %-5d %-10s %-15s %-25s %s\n",
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getInt("age"),
                        resultSet.getString("gender"),
                        resultSet.getString("phone"),
                        resultSet.getString("email"),
                        resultSet.getString("address"));
                }
                
                if (!hasData) {
                    System.out.println("No patients found with name: " + name);
                }
                System.out.println("=".repeat(100));
            }
        } catch (SQLException e) {
            System.err.println("Error searching patient: " + e.getMessage());
        }
    }
    
    /**
     * Update patient details
     */
    public void updatePatient() {
        System.out.print("Enter patient ID to update: ");
        while (!scanner.hasNextInt()) {
            System.out.print("Invalid input. Please enter a valid patient ID: ");
            scanner.next();
        }
        int patientId = scanner.nextInt();
        
        // First check if patient exists
        if (!getPatientById(patientId)) {
            System.out.println("\n✗ Patient with ID " + patientId + " not found!");
            return;
        }
        
        scanner.nextLine(); // Consume newline
        System.out.println("\n--- Enter new details (press Enter to keep current value) ---");
        
        System.out.print("Enter new name: ");
        String name = scanner.nextLine();
        
        System.out.print("Enter new age: ");
        String ageInput = scanner.nextLine();
        Integer age = ageInput.isEmpty() ? null : Integer.parseInt(ageInput);
        
        System.out.print("Enter new gender: ");
        String gender = scanner.nextLine();
        
        System.out.print("Enter new phone: ");
        String phone = scanner.nextLine();
        
        System.out.print("Enter new email: ");
        String email = scanner.nextLine();
        
        System.out.print("Enter new address: ");
        String address = scanner.nextLine();
        
        String query = "UPDATE patients SET name = COALESCE(NULLIF(?, ''), name), " +
                      "age = COALESCE(?, age), gender = COALESCE(NULLIF(?, ''), gender), " +
                      "phone = COALESCE(NULLIF(?, ''), phone), email = COALESCE(NULLIF(?, ''), email), " +
                      "address = COALESCE(NULLIF(?, ''), address) WHERE id = ?";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, name);
            preparedStatement.setObject(2, age);
            preparedStatement.setString(3, gender);
            preparedStatement.setString(4, phone);
            preparedStatement.setString(5, email);
            preparedStatement.setString(6, address);
            preparedStatement.setInt(7, patientId);
            
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("\n✓ Patient updated successfully!");
            } else {
                System.out.println("\n✗ Failed to update patient!");
            }
        } catch (SQLException e) {
            System.err.println("Error updating patient: " + e.getMessage());
        }
    }
    
    /**
     * Delete patient by ID
     */
    public void deletePatient() {
        System.out.print("Enter patient ID to delete: ");
        while (!scanner.hasNextInt()) {
            System.out.print("Invalid input. Please enter a valid patient ID: ");
            scanner.next();
        }
        int patientId = scanner.nextInt();
        
        // First check if patient exists
        if (!getPatientById(patientId)) {
            System.out.println("\n✗ Patient with ID " + patientId + " not found!");
            return;
        }
        
        System.out.print("Are you sure you want to delete this patient? (yes/no): ");
        String confirm = scanner.next();
        
        if (!confirm.equalsIgnoreCase("yes")) {
            System.out.println("Deletion cancelled.");
            return;
        }
        
        String query = "DELETE FROM patients WHERE id = ?";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, patientId);
            
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("\n✓ Patient deleted successfully!");
            } else {
                System.out.println("\n✗ Failed to delete patient!");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting patient: " + e.getMessage());
        }
    }
    
    /**
     * Check if patient exists by ID
     * @param patientId patient ID to check
     * @return true if patient exists, false otherwise
     */
    public boolean getPatientById(int patientId) {
        String query = "SELECT id FROM patients WHERE id = ?";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, patientId);
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking patient: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get patient name by ID
     * @param patientId patient ID
     * @return patient name or null if not found
     */
    public String getPatientNameById(int patientId) {
        String query = "SELECT name FROM patients WHERE id = ?";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, patientId);
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("name");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting patient name: " + e.getMessage());
        }
        return null;
    }
}