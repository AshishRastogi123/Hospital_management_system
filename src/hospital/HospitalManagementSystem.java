package hospital;

import hospital.model.Patient;
import hospital.model.Doctor;
import hospital.model.Appointment;
import hospital.model.Prescription;
import hospital.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * HospitalManagementSystem - Main class for the Hospital Management System
 * Provides a menu-driven console interface for managing hospital operations
 */
public class HospitalManagementSystem {
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Connection connection = null;
        
        try {
            // Get database connection using singleton
            DatabaseConnection dbConnection = DatabaseConnection.getInstance();
            connection = dbConnection.getConnection();
            
            System.out.println("\n" + "=".repeat(60));
            System.out.println("   WELCOME TO HOSPITAL MANAGEMENT SYSTEM");
            System.out.println("=".repeat(60));
            System.out.println("✓ Database connected successfully!\n");
            
            // Initialize model classes
            Patient patient = new Patient(connection, scanner);
            Doctor doctor = new Doctor(connection);
            Appointment appointment = new Appointment(connection, scanner);
            Prescription prescription = new Prescription(connection, scanner);
            
            // Main menu loop
            boolean running = true;
            while (running) {
                displayMainMenu();
                System.out.print("Enter your choice: ");
                
                // Validate input is an integer
                if (!scanner.hasNextInt()) {
                    System.out.println("\n✗ Invalid input! Please enter a number.");
                    scanner.next(); // Clear invalid input
                    continue;
                }
                
                int choice = scanner.nextInt();
                System.out.println();
                
                switch (choice) {
                    case 1:
                        // Add Patient
                        patient.addPatient();
                        break;
                        
                    case 2:
                        // View Patients
                        patient.viewPatients();
                        break;
                        
                    case 3:
                        // Search Patient
                        displaySearchMenu();
                        if (!scanner.hasNextInt()) {
                            System.out.println("\n✗ Invalid input!");
                            scanner.next();
                            break;
                        }
                        int searchChoice = scanner.nextInt();
                        switch (searchChoice) {
                            case 1:
                                patient.searchPatientById();
                                break;
                            case 2:
                                patient.searchPatientByName();
                                break;
                            default:
                                System.out.println("\n✗ Invalid search option!");
                        }
                        break;
                        
                    case 4:
                        // Update Patient
                        patient.updatePatient();
                        break;
                        
                    case 5:
                        // Delete Patient
                        patient.deletePatient();
                        break;
                        
                    case 6:
                        // View Doctors
                        doctor.viewDoctors();
                        break;
                        
                    case 7:
                        // Book Appointment
                        appointment.bookAppointment();
                        break;
                        
                    case 8:
                        // View Appointments
                        appointment.viewAppointments();
                        break;
                        
                    case 9:
                        // Cancel Appointment
                        appointment.cancelAppointment();
                        break;
                        
                    case 10:
                        // Reschedule Appointment
                        appointment.rescheduleAppointment();
                        break;
                        
                    case 11:
                        // Check Doctor Availability
                        appointment.checkDoctorAvailability();
                        break;
                        
                    case 12:
                        // Add Prescription
                        prescription.addPrescription();
                        break;
                        
                    case 13:
                        // View Prescriptions
                        displayPrescriptionMenu();
                        if (!scanner.hasNextInt()) {
                            System.out.println("\n✗ Invalid input!");
                            scanner.next();
                            break;
                        }
                        int prescriptionChoice = scanner.nextInt();
                        switch (prescriptionChoice) {
                            case 1:
                                prescription.viewPrescriptions();
                                break;
                            case 2:
                                prescription.viewPrescriptionHistory();
                                break;
                            default:
                                System.out.println("\n✗ Invalid option!");
                        }
                        break;
                        
                    case 14:
                        // Exit
                        running = false;
                        System.out.println("Thank you for using Hospital Management System!");
                        System.out.println("Goodbye!");
                        break;
                        
                    default:
                        System.out.println("\n✗ Invalid choice! Please enter a number between 1 and 14.");
                }
                
                if (running) {
                    System.out.println("\nPress Enter to continue...");
                    scanner.nextLine();
                    scanner.nextLine(); // Wait for Enter key
                }
            }
            
        } catch (SQLException e) {
            System.err.println("\n✗ Database connection failed: " + e.getMessage());
            System.err.println("Please ensure:");
            System.err.println("  1. MySQL server is running");
            System.err.println("  2. Database 'hospital' exists");
            System.err.println("  3. Tables are created (run schema.sql)");
        } catch (Exception e) {
            System.err.println("\n✗ An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Clean up resources
            if (connection != null) {
                try {
                    connection.close();
                    System.out.println("\n✓ Database connection closed.");
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
            scanner.close();
        }
    }
    
    /**
     * Display the main menu
     */
    private static void displayMainMenu() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("           HOSPITAL MANAGEMENT SYSTEM");
        System.out.println("=".repeat(60));
        System.out.println("  1.  Add Patient");
        System.out.println("  2.  View Patients");
        System.out.println("  3.  Search Patient");
        System.out.println("  4.  Update Patient");
        System.out.println("  5.  Delete Patient");
        System.out.println("  6.  View Doctors");
        System.out.println("  7.  Book Appointment");
        System.out.println("  8.  View Appointments");
        System.out.println("  9.  Cancel Appointment");
        System.out.println("  10. Reschedule Appointment");
        System.out.println("  11. Check Doctor Availability");
        System.out.println("  12. Add Prescription");
        System.out.println("  13. View Prescriptions");
        System.out.println("  14. Exit");
        System.out.println("=".repeat(60));
    }
    
    /**
     * Display search sub-menu
     */
    private static void displaySearchMenu() {
        System.out.println("\n--- Search Patient ---");
        System.out.println("  1. Search by ID");
        System.out.println("  2. Search by Name");
        System.out.print("Enter your choice: ");
    }
    
    /**
     * Display prescription sub-menu
     */
    private static void displayPrescriptionMenu() {
        System.out.println("\n--- View Prescriptions ---");
        System.out.println("  1. View All Prescriptions");
        System.out.println("  2. View Patient Prescription History");
        System.out.print("Enter your choice: ");
    }
}