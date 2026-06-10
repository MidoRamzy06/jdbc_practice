import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class WebServer {
    private static final int PORT = 8080;

    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
            
            // Register contexts
            server.createContext("/", new StaticHandler());
            server.createContext("/api/status", new ApiHandler());
            server.createContext("/api/tables", new ApiHandler());
            server.createContext("/api/data", new ApiHandler());
            server.createContext("/api/query", new ApiHandler());
            server.createContext("/api/insights/salaries", new ApiHandler());
            server.createContext("/api/insights/assignments", new ApiHandler());
            
            server.setExecutor(null); // default executor
            System.out.println("=================================================");
            System.out.println("        COMPANYDB WEB EXPLORER STARTED           ");
            System.out.println("=================================================");
            System.out.println("Server is running on: http://localhost:" + PORT);
            System.out.println("Press Ctrl+C to stop the server.");
            System.out.println("=================================================");
            
            // Automatically open browser
            openBrowser("http://localhost:" + PORT);
            
            server.start();
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }

    private static void openBrowser(String url) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec("open " + url);
            } else {
                Runtime.getRuntime().exec("xdg-open " + url);
            }
        } catch (Exception e) {
            // Ignore browser opening failures
        }
    }

    // Static assets handler
    static class StaticHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                path = "/index.html";
            }
            
            java.io.File file = new java.io.File("web" + path);
            if (!file.exists()) {
                file = new java.io.File(path.substring(1)); // try root directory
            }
            
            if (file.exists() && !file.isDirectory()) {
                byte[] bytes = Files.readAllBytes(file.toPath());
                String contentType = "text/html";
                if (path.endsWith(".css")) contentType = "text/css";
                else if (path.endsWith(".js")) contentType = "application/javascript";
                else if (path.endsWith(".svg")) contentType = "image/svg+xml";
                
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } else {
                String response = "404 Not Found";
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }
    }

    // API endpoints handler
    static class ApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            
            String path = exchange.getRequestURI().getPath();
            String response = "";
            int statusCode = 200;
            
            try (Connection conn = DBConnection.getConnection()) {
                if (path.equals("/api/status")) {
                    response = "{\"status\":\"connected\",\"database\":\"CompanyDB\"}";
                } else if (path.equals("/api/tables")) {
                    String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE' AND TABLE_NAME != 'sysdiagrams' ORDER BY TABLE_NAME";
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery(sql)) {
                        response = resultSetToJson(rs);
                    }
                } else if (path.equals("/api/data")) {
                    String query = exchange.getRequestURI().getQuery();
                    String tableName = "";
                    if (query != null && query.contains("table=")) {
                        tableName = query.split("table=")[1].split("&")[0];
                        tableName = java.net.URLDecoder.decode(tableName, "UTF-8");
                    }
                    
                    if (isValidTableName(tableName)) {
                        String sql = "SELECT * FROM " + tableName;
                        try (Statement stmt = conn.createStatement();
                             ResultSet rs = stmt.executeQuery(sql)) {
                            response = resultSetToJson(rs);
                        }
                    } else {
                        response = "{\"error\":\"Invalid or unauthorized table name\"}";
                        statusCode = 400;
                    }
                } else if (path.equals("/api/query")) {
                    String query = exchange.getRequestURI().getQuery();
                    String sql = "";
                    if (query != null && query.contains("sql=")) {
                        sql = query.split("sql=")[1].split("&")[0];
                        sql = java.net.URLDecoder.decode(sql, "UTF-8");
                    }
                    
                    if (!sql.trim().isEmpty()) {
                        try (Statement stmt = conn.createStatement()) {
                            boolean isResultSet = stmt.execute(sql);
                            if (isResultSet) {
                                try (ResultSet rs = stmt.getResultSet()) {
                                    response = resultSetToJson(rs);
                                }
                            } else {
                                int updateCount = stmt.getUpdateCount();
                                response = "{\"message\":\"Query executed successfully.\",\"rowsAffected\":" + updateCount + "}";
                            }
                        }
                    } else {
                        response = "{\"error\":\"No SQL query provided\"}";
                        statusCode = 400;
                    }
                } else if (path.equals("/api/insights/salaries")) {
                    String sql = "SELECT d.name AS Department, COUNT(e.employee_id) AS [Employees Count], " +
                                 "FORMAT(AVG(e.salary), 'N2') AS [Avg Salary], FORMAT(SUM(e.salary), 'N2') AS [Total Budget] " +
                                 "FROM Departments d " +
                                 "LEFT JOIN Employees e ON d.department_id = e.department_id " +
                                 "GROUP BY d.name ORDER BY AVG(e.salary) DESC";
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery(sql)) {
                        response = resultSetToJson(rs);
                    }
                } else if (path.equals("/api/insights/assignments")) {
                    String sql = "SELECT e.first_name + ' ' + e.last_name AS Employee, p.name AS Project, ep.role AS Role " +
                                 "FROM EmployeeProjects ep " +
                                 "JOIN Employees e ON ep.employee_id = e.employee_id " +
                                 "JOIN Projects p ON ep.project_id = p.project_id " +
                                 "ORDER BY p.name, ep.role";
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery(sql)) {
                        response = resultSetToJson(rs);
                    }
                } else {
                    response = "{\"error\":\"Endpoint not found\"}";
                    statusCode = 404;
                }
            } catch (Exception e) {
                response = "{\"status\":\"error\",\"message\":\"" + escapeJson(e.getMessage()) + "\"}";
                if (path.equals("/api/status")) {
                    statusCode = 200; // Return 200 even on connection error so page gets structured error JSON
                } else {
                    statusCode = 500;
                }
            }
            
            byte[] bytes = response.getBytes("UTF-8");
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
        
        private boolean isValidTableName(String tableName) {
            return tableName.equals("Departments") || 
                   tableName.equals("Employees") || 
                   tableName.equals("Projects") || 
                   tableName.equals("EmployeeProjects");
        }

        private String resultSetToJson(ResultSet rs) throws SQLException {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            boolean firstRow = true;
            
            while (rs.next()) {
                if (!firstRow) sb.append(",");
                sb.append("{");
                for (int i = 1; i <= colCount; i++) {
                    if (i > 1) sb.append(",");
                    String label = meta.getColumnLabel(i);
                    String val = rs.getString(i);
                    sb.append("\"").append(escapeJson(label)).append("\":");
                    if (val == null) {
                        sb.append("null");
                    } else {
                        sb.append("\"").append(escapeJson(val)).append("\"");
                    }
                }
                sb.append("}");
                firstRow = false;
            }
            sb.append("]");
            return sb.toString();
        }

        private String escapeJson(String str) {
            if (str == null) return "";
            return str.replace("\\", "\\\\")
                      .replace("\"", "\\\"")
                      .replace("\b", "\\b")
                      .replace("\f", "\\f")
                      .replace("\n", "\\n")
                      .replace("\r", "\\r")
                      .replace("\t", "\\t");
        }
    }
}
