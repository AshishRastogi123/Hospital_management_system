package hospital.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Doctor - Model class for Doctor entity
 * Handles all doctor-related database operations
 */
public class Doctor {
    
    private Connection connection;
    
    // Constructor
    public Doctor(Connection connection) {
        this.connection = connection;
    }
    
    /**
     * View all doctors from the database
     */
    public void viewDoctors() {
        String query = "SELECT * FROM doctors ORDER BY id";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            
            System.out.println("\n" + "=".repeat(110));
            System.out.printf("%-5s %-25s %-20s %-15s %-25s %-10s\n", 
                "ID", "Name", "Specialty", "Phone", "Email", "Experience");
            System.out.println("=".repeat(110));
            
            boolean hasData = false;
            while (resultSet.next()) {
                hasData = true;
                System.out.printf("%-5d %-25s %-20s %-15s %-25s %-10d\n",
                    resultSet.getInt("id"),
                    resultSet.getString("name"),
                    resultSet.getString("specialty"),
                    resultSet.getString("phone"),
                    resultSet.getString("email"),
                    resultSet.getInt("experience_years"));
            }
            
            if (!hasData) {
                System.out.println("No doctors found!");
            }
            System.out.println("=".repeat(110));
            
        } catch (SQLException e) {
            System.err.println("Error viewing doctors: " + e.getMessage());
        }
    }
    
    /**
     * View doctor by ID
     */
    public void viewDoctorById(int doctorId) {
        String query = "SELECT * FROM doctors WHERE id = ?";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, doctorId);
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    System.out.println("\n" + "=".repeat(80));
                    System.out.println("Doctor Details:");
                    System.out.println("=".repeat(80));
                    System.out.println("ID:             " + resultSet.getInt("id"));
                    System.out.println("Name:           " + resultSet.getString("name"));
                    System.out.println("Specialty:      " + resultSet.getString("specialty"));
                    System.out.println("Phone:          " + resultSet.getString("phone"));
                    System.out.println("Email:          " + resultSet.getString("email"));
                    System.out.println("Experience:     " + resultSet.getInt("experience_years") + " years");
                    System.out.println("=".repeat(80));
                } else {
                    System.out.println("\n✗ Doctor with ID " + doctorId + " not found!");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error viewing doctor: " + e.getMessage());
        }
    }
    
    /**
     * Check if doctor exists by ID
     * @param doctorId doctor ID to check
     * @return true if doctor exists, false otherwise
     */
    public boolean getDoctorById(int doctorId) {
        String query = "SELECT id FROM doctors WHERE id = ?";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, doctorId);
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking doctor: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get doctor name by ID
     * @param doctorId doctor ID
     * @return doctor name or null if not found
     */
    public String getDoctorNameById(int doctorId) {
        String query = "SELECT name FROM doctors WHERE id = ?";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, doctorId);
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("name");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting doctor name: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Get doctor specialty by ID
     * @param doctorId doctor ID
     * @return doctor specialty or null if not found
     */
    public String getDoctorSpecialtyById(int doctorId) {
        String query = "SELECT specialty FROM doctors WHERE id = ?";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, doctorId);
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("specialty");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting doctor specialty: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * View doctors by specialty
     */
    public void viewDoctorsBySpecialty(String specialty) {
        String query = "SELECT * FROM doctors WHERE specialty = ?";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, specialty);
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                System.out.println("\n" + "=".repeat(110));
                System.out.printf("%-5s %-25s %-20s %-15s %-25s %-10s\n", 
                    "ID", "Name", "Specialty", "Phone", "Email", "Experience");
                System.out.println("=".repeat(110));
                
                boolean hasData = false;
                while (resultSet.next()) {
                    hasData = true;
                    System.out.printf("%-5d %-25s %-20s %-15s %-25s %-10d\n",
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("specialty"),
                        resultSet.getString("phone"),
                        resultSet.getString("email"),
                        resultSet.getInt("experience_years"));
                }
                
                if (!hasData) {
                    System.out.println("No doctors found with specialty: " + specialty);
                }
                System.out.println("=".repeat(110));
            }
        } catch (SQLException e) {
            System.err.println("Error viewing doctors by specialty: " + e.getMessage());
        }
    }
}