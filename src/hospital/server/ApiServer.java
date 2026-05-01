package hospital.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import hospital.util.DatabaseConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ApiServer {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        server.createContext("/patients", new GetPatientsHandler());
        server.createContext("/add-patient", new AddPatientHandler());
        server.createContext("/delete-patient", new DeletePatientHandler());
        server.createContext("/doctors", new GetDoctorsHandler());
        server.createContext("/add-doctor", new AddDoctorHandler());
        server.createContext("/appointments", new GetAppointmentsHandler());
        server.createContext("/book-appointment", new BookAppointmentHandler());
        server.createContext("/update-patient", new UpdatePatientHandler());
        server.createContext("/reschedule-appointment", new RescheduleAppointmentHandler());
        
        server.setExecutor(null);
        System.out.println("API Server is starting on http://localhost:8080");
        server.start();
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS, DELETE");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] bytes = response.getBytes("UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    static class GetPatientsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }

            if ("GET".equals(exchange.getRequestMethod())) {
                StringBuilder jsonResponse = new StringBuilder("[");
                try {
                    Connection conn = DatabaseConnection.getInstance().getConnection();
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT id, name, age, gender, phone FROM patients");
                    boolean first = true;
                    while (rs.next()) {
                        if (!first) jsonResponse.append(",");
                        jsonResponse.append(String.format("{\"id\":%d, \"name\":\"%s\", \"age\":%d, \"gender\":\"%s\", \"phone\":\"%s\"}", 
                            rs.getInt("id"), escapeJson(rs.getString("name")), rs.getInt("age"), 
                            escapeJson(rs.getString("gender")), escapeJson(rs.getString("phone") == null ? "" : rs.getString("phone"))));
                        first = false;
                    }
                    jsonResponse.append("]");
                    sendResponse(exchange, 200, jsonResponse.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    sendResponse(exchange, 500, "{\"error\": \"Database error\"}");
                }
            }
        }
    }

    static class AddPatientHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }

            if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    String body = new String(exchange.getRequestBody().readAllBytes());
                    String name = extractJsonValue(body, "name");
                    int age = Integer.parseInt(extractJsonValue(body, "age"));
                    String gender = extractJsonValue(body, "gender");
                    String phone = extractJsonValue(body, "phone");
                    
                    if(gender.isEmpty()) gender = "Other";

                    Connection conn = DatabaseConnection.getInstance().getConnection();
                    String sql = "INSERT INTO patients (name, age, gender, phone) VALUES (?, ?, ?, ?)"; 
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, name);
                    pstmt.setInt(2, age);
                    pstmt.setString(3, gender);
                    pstmt.setString(4, phone);
                    
                    if (pstmt.executeUpdate() > 0) {
                        sendResponse(exchange, 201, "{\"message\": \"Success\"}");
                    } else {
                        throw new Exception("Insert failed");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    sendResponse(exchange, 500, "{\"error\": \"Failed to add patient\"}");
                }
            }
        }
    }

    static class DeletePatientHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }

            if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    String body = new String(exchange.getRequestBody().readAllBytes());
                    int id = Integer.parseInt(extractJsonValue(body, "id"));
                    
                    Connection conn = DatabaseConnection.getInstance().getConnection();
                    String sql = "DELETE FROM patients WHERE id = ?"; 
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, id);
                    
                    pstmt.executeUpdate();
                    sendResponse(exchange, 200, "{\"message\": \"Success\"}");
                } catch (Exception e) {
                    e.printStackTrace();
                    sendResponse(exchange, 500, "{\"error\": \"Failed to delete patient\"}");
                }
            }
        }
    }

    static class GetDoctorsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }

            if ("GET".equals(exchange.getRequestMethod())) {
                StringBuilder jsonResponse = new StringBuilder("[");
                try {
                    Connection conn = DatabaseConnection.getInstance().getConnection();
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT id, name, specialty, phone FROM doctors");
                    boolean first = true;
                    while (rs.next()) {
                        if (!first) jsonResponse.append(",");
                        jsonResponse.append(String.format("{\"id\":%d, \"name\":\"%s\", \"specialty\":\"%s\", \"phone\":\"%s\"}", 
                            rs.getInt("id"), escapeJson(rs.getString("name")), 
                            escapeJson(rs.getString("specialty")), 
                            escapeJson(rs.getString("phone") == null ? "" : rs.getString("phone"))));
                        first = false;
                    }
                    jsonResponse.append("]");
                    sendResponse(exchange, 200, jsonResponse.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    sendResponse(exchange, 500, "{\"error\": \"Database error\"}");
                }
            }
        }
    }

    static class AddDoctorHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }

            if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    String body = new String(exchange.getRequestBody().readAllBytes());
                    String name = extractJsonValue(body, "name");
                    String specialty = extractJsonValue(body, "specialty");
                    String phone = extractJsonValue(body, "phone");
                    String email = extractJsonValue(body, "email");
                    
                    Connection conn = DatabaseConnection.getInstance().getConnection();
                    String sql = "INSERT INTO doctors (name, specialty, phone, email) VALUES (?, ?, ?, ?)"; 
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, name);
                    pstmt.setString(2, specialty);
                    pstmt.setString(3, phone);
                    pstmt.setString(4, email);
                    
                    if (pstmt.executeUpdate() > 0) {
                        sendResponse(exchange, 201, "{\"message\": \"Success\"}");
                    } else {
                        throw new Exception("Insert failed");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    sendResponse(exchange, 500, "{\"error\": \"Failed to add doctor\"}");
                }
            }
        }
    }

    static class GetAppointmentsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }

            if ("GET".equals(exchange.getRequestMethod())) {
                StringBuilder jsonResponse = new StringBuilder("[");
                try {
                    Connection conn = DatabaseConnection.getInstance().getConnection();
                    Statement stmt = conn.createStatement();
                    String sql = "SELECT a.id, p.name as patient_name, d.name as doctor_name, a.appointment_date, a.appointment_time, a.status " +
                                 "FROM appointments a " +
                                 "JOIN patients p ON a.patient_id = p.id " +
                                 "JOIN doctors d ON a.doctor_id = d.id " +
                                 "ORDER BY a.appointment_date DESC, a.appointment_time DESC";
                    ResultSet rs = stmt.executeQuery(sql);
                    boolean first = true;
                    while (rs.next()) {
                        if (!first) jsonResponse.append(",");
                        jsonResponse.append(String.format("{\"id\":%d, \"patient\":\"%s\", \"doctor\":\"%s\", \"date\":\"%s\", \"time\":\"%s\", \"status\":\"%s\"}", 
                            rs.getInt("id"), escapeJson(rs.getString("patient_name")), 
                            escapeJson(rs.getString("doctor_name")), 
                            escapeJson(rs.getString("appointment_date")),
                            escapeJson(rs.getString("appointment_time")),
                            escapeJson(rs.getString("status"))));
                        first = false;
                    }
                    jsonResponse.append("]");
                    sendResponse(exchange, 200, jsonResponse.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    sendResponse(exchange, 500, "{\"error\": \"Database error\"}");
                }
            }
        }
    }

    static class BookAppointmentHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }

            if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    String body = new String(exchange.getRequestBody().readAllBytes());
                    int patientId = Integer.parseInt(extractJsonValue(body, "patient_id"));
                    int doctorId = Integer.parseInt(extractJsonValue(body, "doctor_id"));
                    String date = extractJsonValue(body, "date");
                    String time = extractJsonValue(body, "time");
                    
                    Connection conn = DatabaseConnection.getInstance().getConnection();
                    String sql = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, status) VALUES (?, ?, ?, ?, 'SCHEDULED')"; 
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, patientId);
                    pstmt.setInt(2, doctorId);
                    pstmt.setString(3, date);
                    pstmt.setString(4, time);
                    
                    if (pstmt.executeUpdate() > 0) {
                        sendResponse(exchange, 201, "{\"message\": \"Success\"}");
                    } else {
                        throw new Exception("Insert failed");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    sendResponse(exchange, 500, "{\"error\": \"Failed to book appointment\"}");
                }
            }
        }
    }

    static class UpdatePatientHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }

            if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    String body = new String(exchange.getRequestBody().readAllBytes());
                    int id = Integer.parseInt(extractJsonValue(body, "id"));
                    String name = extractJsonValue(body, "name");
                    int age = Integer.parseInt(extractJsonValue(body, "age"));
                    String gender = extractJsonValue(body, "gender");
                    String phone = extractJsonValue(body, "phone");
                    
                    if(gender.isEmpty()) gender = "Other";

                    Connection conn = DatabaseConnection.getInstance().getConnection();
                    String sql = "UPDATE patients SET name=?, age=?, gender=?, phone=? WHERE id=?"; 
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, name);
                    pstmt.setInt(2, age);
                    pstmt.setString(3, gender);
                    pstmt.setString(4, phone);
                    pstmt.setInt(5, id);
                    
                    if (pstmt.executeUpdate() > 0) {
                        sendResponse(exchange, 200, "{\"message\": \"Success\"}");
                    } else {
                        throw new Exception("Update failed");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    sendResponse(exchange, 500, "{\"error\": \"Failed to update patient\"}");
                }
            }
        }
    }

    static class RescheduleAppointmentHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }

            if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    String body = new String(exchange.getRequestBody().readAllBytes());
                    int id = Integer.parseInt(extractJsonValue(body, "id"));
                    String date = extractJsonValue(body, "date");
                    String time = extractJsonValue(body, "time");
                    
                    Connection conn = DatabaseConnection.getInstance().getConnection();
                    String sql = "UPDATE appointments SET appointment_date=?, appointment_time=?, status='RESCHEDULED' WHERE id=?"; 
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, date);
                    pstmt.setString(2, time);
                    pstmt.setInt(3, id);
                    
                    if (pstmt.executeUpdate() > 0) {
                        sendResponse(exchange, 200, "{\"message\": \"Success\"}");
                    } else {
                        throw new Exception("Reschedule failed");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    sendResponse(exchange, 500, "{\"error\": \"Failed to reschedule appointment\"}");
                }
            }
        }
    }
    
    private static String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return "";
        int colonIndex = json.indexOf(":", keyIndex);
        int commaIndex = json.indexOf(",", colonIndex);
        int braceIndex = json.indexOf("}", colonIndex);
        int endIndex = (commaIndex != -1 && commaIndex < braceIndex) ? commaIndex : braceIndex;
        String value = json.substring(colonIndex + 1, endIndex).trim();
        if (value.startsWith("\"") && value.endsWith("\"")) value = value.substring(1, value.length() - 1);
        return value;
    }

    private static String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
