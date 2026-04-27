package hospital.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnection - Utility class for managing database connections
 * Implements Singleton pattern for centralized connection management
 */
public class DatabaseConnection {
    
    // Private static instance for Singleton pattern
    private static DatabaseConnection instance;
    
    // Database configuration
    private static final String URL = "jdbc:mysql://localhost:3306/hospital";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "ashish123";
    
    // JDBC Driver class name
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    
    private Connection connection;
    
    // Private constructor to prevent direct instantiation
    private DatabaseConnection() {
        try {
            // Load the JDBC driver
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
        }
    }
    
    /**
     * Get the singleton instance of DatabaseConnection
     * @return DatabaseConnection instance
     */
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }
    
    /**
     * Establish and return a database connection
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        }
        return connection;
    }
    
    /**
     * Close the database connection
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("Database connection closed successfully.");
                }
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
    
    /**
     * Check if the connection is valid
     * @return true if connection is active, false otherwise
     */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Test the database connection
     * @return true if connection successful, false otherwise
     */
    public boolean testConnection() {
        try {
            Connection testConn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            boolean isValid = testConn.isValid(5);
            testConn.close();
            return isValid;
        } catch (SQLException e) {
            System.err.println("Connection test failed: " + e.getMessage());
            return false;
        }
    }
}