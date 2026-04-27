package hospital.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Appointment - Model class for Appointment entity
 * Handles all appointment-related database operations
 * Includes time slot booking and double-booking prevention
 */
public class Appointment {
    
    private Connection connection;
    private Scanner scanner;
    
    // Available time slots (30-minute intervals)
    private static final String[] TIME_SLOTS = {
        "09:00:00", "09:30:00", "10:00:00", "10:30:00", "11:00:00", "11:30:00",
        "14:00:00", "14:30:00", "15:00:00", "15:30:00", "16:00:00", "16:30:00",
        "17:00:00", "17:30:00"
    };
    
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter TIME_INPUT_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");
    
    // Constructor
    public Appointment(Connection connection, Scanner scanner) {
        this.connection = connection;
        this.scanner = scanner;
    }
    
    /**
     * Book a new appointment with time slot
     */
    public void bookAppointment() {
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
            
            // Get appointment date
            System.out.print("Enter appointment date (YYYY-MM-DD): ");
            String dateInput = scanner.next();
            LocalDate appointmentDate;
            try {
                appointmentDate = LocalDate.parse(dateInput);
            } catch (DateTimeParseException e) {
                System.out.println("\n✗ Invalid date format! Please use YYYY-MM-DD format.");
                return;
            }
            
            // Check if date is not in the past
            if (appointmentDate.isBefore(LocalDate.now())) {
                System.out.println("\n✗ Cannot book appointments in the past!");
                return;
            }
            
            // Show available time slots
            showAvailableTimeSlots(doctorId, appointmentDate);
            
            // Get time slot from user
            String timeSlot = getTimeSlotFromUser(doctorId, appointmentDate);
            if (timeSlot == null) {
                return; // User cancelled or invalid input
            }
            
            // Try to book the appointment (with double-booking check)
            if (bookAppointmentWithCheck(patientId, doctorId, appointmentDate, timeSlot)) {
                System.out.println("\n✓ Appointment booked successfully!");
                System.out.println("  Date: " + appointmentDate + " Time: " + timeSlot);
            } else {
                System.out.println("\n✗ Failed to book appointment!");
            }
            
        } catch (Exception e) {
            System.err.println("Error booking appointment: " + e.getMessage());
        }
    }
    
    /**
     * Show available time slots for a doctor on a given date
     */
    private void showAvailableTimeSlots(int doctorId, LocalDate appointmentDate) {
        System.out.println("\n--- Available Time Slots for " + appointmentDate + " ---");
        
        List<String> availableSlots = new ArrayList<>();
        List<String> bookedSlots = getBookedSlots(doctorId, appointmentDate);
        
        System.out.println("\nSlotted Time        Status");
        System.out.println("-".repeat(40));
        
        for (int i = 0; i < TIME_SLOTS.length; i++) {
            String slot = TIME_SLOTS[i];
            LocalTime time = LocalTime.parse(slot, TIME_FORMAT);
            String formattedTime = time.format(TIME_INPUT_FORMAT);
            
            if (bookedSlots.contains(slot)) {
                System.out.printf("%2d. [%s] - BOOKED\n", i + 1, formattedTime);
            } else {
                System.out.printf("%2d. [%s] - AVAILABLE\n", i + 1, formattedTime);
                availableSlots.add(slot);
            }
        }
        
        System.out.println("-".repeat(40));
    }
    
    /**
     * Get booked slots for a doctor on a given date
     */
    private List<String> getBookedSlots(int doctorId, LocalDate appointmentDate) {
        List<String> bookedSlots = new ArrayList<>();
        String query = "SELECT appointment_time FROM appointments WHERE doctor_id = ? AND appointment_date = ? AND status != 'CANCELLED'";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, doctorId);
            preparedStatement.setString(2, appointmentDate.toString());
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    bookedSlots.add(resultSet.getString("appointment_time"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting booked slots: " + e.getMessage());
        }
        
        return bookedSlots;
    }
    
    /**
     * Get time slot from user with double-booking prevention
     */
    private String getTimeSlotFromUser(int doctorId, LocalDate appointmentDate) {
        List<String> availableSlots = new ArrayList<>();
        List<String> bookedSlots = getBookedSlots(doctorId, appointmentDate);
        
        for (String slot : TIME_SLOTS) {
            if (!bookedSlots.contains(slot)) {
                availableSlots.add(slot);
            }
        }
        
        if (availableSlots.isEmpty()) {
            System.out.println("\n✗ No available time slots for this doctor on the selected date!");
            return null;
        }
        
        System.out.print("\nSelect time slot number: ");
        while (!scanner.hasNextInt()) {
            System.out.print("Invalid input. Please enter a valid slot number: ");
            scanner.next();
        }
        int choice = scanner.nextInt();
        
        if (choice < 1 || choice > availableSlots.size()) {
            System.out.println("\n✗ Invalid selection!");
            return null;
        }
        
        return availableSlots.get(choice - 1);
    }
    
    /**
     * Book appointment with double-booking check
     */
    private boolean bookAppointmentWithCheck(int patientId, int doctorId, LocalDate appointmentDate, String timeSlot) {
        // Check for double booking (same doctor, same time)
        String checkDoctorQuery = "SELECT COUNT(*) FROM appointments WHERE doctor_id = ? AND appointment_date = ? AND appointment_time = ? AND status != 'CANCELLED'";
        
        try (PreparedStatement checkStmt = connection.prepareStatement(checkDoctorQuery)) {
            checkStmt.setInt(1, doctorId);
            checkStmt.setString(2, appointmentDate.toString());
            checkStmt.setString(3, timeSlot);
            
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("\n✗ This time slot is already booked!");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking doctor availability: " + e.getMessage());
            return false;
        }
        
        // Check for same patient duplicate appointment
        String checkPatientQuery = "SELECT COUNT(*) FROM appointments WHERE patient_id = ? AND appointment_date = ? AND appointment_time = ? AND status != 'CANCELLED'";
        
        try (PreparedStatement checkStmt = connection.prepareStatement(checkPatientQuery)) {
            checkStmt.setInt(1, patientId);
            checkStmt.setString(2, appointmentDate.toString());
            checkStmt.setString(3, timeSlot);
            
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("\n✗ You already have an appointment at this time!");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking patient appointment: " + e.getMessage());
            return false;
        }
        
        // Insert the appointment
        String query = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, status) VALUES (?, ?, ?, ?, 'SCHEDULED')";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, patientId);
            preparedStatement.setInt(2, doctorId);
            preparedStatement.setString(3, appointmentDate.toString());
            preparedStatement.setString(4, timeSlot);
            
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting appointment: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * View all appointments
     */
    public void viewAppointments() {
        String query = "SELECT a.id, p.name as patient_name, d.name as doctor_name, " +
                      "a.appointment_date, a.appointment_time, a.status " +
                      "FROM appointments a " +
                      "JOIN patients p ON a.patient_id = p.id " +
                      "JOIN doctors d ON a.doctor_id = d.id " +
                      "ORDER BY a.appointment_date, a.appointment_time";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            
            System.out.println("\n" + "=".repeat(110));
            System.out.printf("%-5s %-20s %-25s %-15s %-15s %-12s\n", 
                "ID", "Patient", "Doctor", "Date", "Time", "Status");
            System.out.println("=".repeat(110));
            
            boolean hasData = false;
            while (resultSet.next()) {
                hasData = true;
                LocalTime time = resultSet.getTime("appointment_time").toLocalTime();
                System.out.printf("%-5d %-20s %-25s %-15s %-15s %-12s\n",
                    resultSet.getInt("id"),
                    resultSet.getString("patient_name"),
                    resultSet.getString("doctor_name"),
                    resultSet.getDate("appointment_date"),
                    time.format(TIME_INPUT_FORMAT),
                    resultSet.getString("status"));
            }
            
            if (!hasData) {
                System.out.println("No appointments found!");
            }
            System.out.println("=".repeat(110));
            
        } catch (SQLException e) {
            System.err.println("Error viewing appointments: " + e.getMessage());
        }
    }
    
    /**
     * Cancel an appointment
     */
    public void cancelAppointment() {
        System.out.print("Enter appointment ID to cancel: ");
        while (!scanner.hasNextInt()) {
            System.out.print("Invalid input. Please enter a valid appointment ID: ");
            scanner.next();
        }
        int appointmentId = scanner.nextInt();
        
        // Check if appointment exists
        if (!isValidAppointment(appointmentId)) {
            System.out.println("\n✗ Appointment with ID " + appointmentId + " not found!");
            return;
        }
        
        System.out.print("Are you sure you want to cancel this appointment? (yes/no): ");
        String confirm = scanner.next();
        
        if (!confirm.equalsIgnoreCase("yes")) {
            System.out.println("Cancellation cancelled.");
            return;
        }
        
        String query = "UPDATE appointments SET status = 'CANCELLED' WHERE id = ?";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, appointmentId);
            
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("\n✓ Appointment cancelled successfully!");
            } else {
                System.out.println("\n✗ Failed to cancel appointment!");
            }
        } catch (SQLException e) {
            System.err.println("Error cancelling appointment: " + e.getMessage());
        }
    }
    
    /**
     * Reschedule an appointment
     */
    public void rescheduleAppointment() {
        System.out.print("Enter appointment ID to reschedule: ");
        while (!scanner.hasNextInt()) {
            System.out.print("Invalid input. Please enter a valid appointment ID: ");
            scanner.next();
        }
        int appointmentId = scanner.nextInt();
        
        // Get current appointment details
        String getQuery = "SELECT doctor_id, appointment_date FROM appointments WHERE id = ?";
        
        int doctorId = 0;
        LocalDate oldDate = null;
        
        try (PreparedStatement ps = connection.prepareStatement(getQuery)) {
            ps.setInt(1, appointmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    doctorId = rs.getInt("doctor_id");
                    oldDate = rs.getDate("appointment_date").toLocalDate();
                } else {
                    System.out.println("\n✗ Appointment with ID " + appointmentId + " not found!");
                    return;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting appointment: " + e.getMessage());
            return;
        }
        
        // Get new date
        System.out.print("Enter new appointment date (YYYY-MM-DD): ");
        String dateInput = scanner.next();
        LocalDate newDate;
        try {
            newDate = LocalDate.parse(dateInput);
        } catch (DateTimeParseException e) {
            System.out.println("\n✗ Invalid date format!");
            return;
        }
        
        if (newDate.isBefore(LocalDate.now())) {
            System.out.println("\n✗ Cannot reschedule to a past date!");
            return;
        }
        
        // Show available time slots
        showAvailableTimeSlots(doctorId, newDate);
        
        // Get new time slot
        String newTimeSlot = getTimeSlotFromUser(doctorId, newDate);
        if (newTimeSlot == null) {
            return;
        }
        
        // Check if new slot is available (excluding current appointment)
        String checkQuery = "SELECT COUNT(*) FROM appointments WHERE doctor_id = ? AND appointment_date = ? AND appointment_time = ? AND id != ? AND status != 'CANCELLED'";
        
        try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, doctorId);
            checkStmt.setString(2, newDate.toString());
            checkStmt.setString(3, newTimeSlot);
            checkStmt.setInt(4, appointmentId);
            
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("\n✗ This time slot is already booked!");
                    return;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking availability: " + e.getMessage());
            return;
        }
        
        // Update the appointment
        String query = "UPDATE appointments SET appointment_date = ?, appointment_time = ? WHERE id = ?";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, newDate.toString());
            preparedStatement.setString(2, newTimeSlot);
            preparedStatement.setInt(3, appointmentId);
            
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("\n✓ Appointment rescheduled successfully!");
                System.out.println("  New Date: " + newDate + " Time: " + newTimeSlot);
            } else {
                System.out.println("\n✗ Failed to reschedule appointment!");
            }
        } catch (SQLException e) {
            System.err.println("Error rescheduling appointment: " + e.getMessage());
        }
    }
    
    /**
     * Check doctor availability for a specific date
     */
    public void checkDoctorAvailability() {
        System.out.print("Enter doctor ID: ");
        while (!scanner.hasNextInt()) {
            System.out.print("Invalid input. Please enter a valid doctor ID: ");
            scanner.next();
        }
        int doctorId = scanner.nextInt();
        
        System.out.print("Enter date (YYYY-MM-DD): ");
        String dateInput = scanner.next();
        LocalDate appointmentDate;
        try {
            appointmentDate = LocalDate.parse(dateInput);
        } catch (DateTimeParseException e) {
            System.out.println("\n✗ Invalid date format!");
            return;
        }
        
        showAvailableTimeSlots(doctorId, appointmentDate);
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
    
    /**
     * Validate appointment exists
     */
    private boolean isValidAppointment(int appointmentId) {
        String query = "SELECT id FROM appointments WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, appointmentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }
}