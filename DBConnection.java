import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Database credentials loaded from environment variables with fallback defaults
    private static final String SERVER = getEnvOrDefault("DB_SERVER", "localhost");
    private static final String PORT = getEnvOrDefault("DB_PORT", "1433");
    private static final String DATABASE = getEnvOrDefault("DB_DATABASE", "CompanyDB");
    private static final String USER = getEnvOrDefault("DB_USER", "java_user");
    private static final String PASSWORD = getEnvOrDefault("DB_PASSWORD", "JavaPassword123!");

    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Establishes a connection to the SQL Server database.
     * Uses: encrypt=true, trustServerCertificate=true, localhost:1433.
     */
    public static Connection getConnection() throws SQLException {
        String url = String.format(
            "jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=true;trustServerCertificate=true;loginTimeout=5;",
            SERVER, PORT, DATABASE
        );

        try {
            System.out.println("Connecting to SQL Server at " + SERVER + ":" + PORT + "...");
            return DriverManager.getConnection(url, USER, PASSWORD);
        } catch (SQLException e) {
            System.out.println("Primary connection failed: " + e.getMessage());
            System.out.println("Attempting fallback to .\\SQLEXPRESS...");

            String fallbackUrl = String.format(
                "jdbc:sqlserver://localhost\\SQLEXPRESS;databaseName=%s;encrypt=true;trustServerCertificate=true;loginTimeout=5;",
                DATABASE
            );

            try {
                return DriverManager.getConnection(fallbackUrl, USER, PASSWORD);
            } catch (SQLException ex) {
                printTroubleshootingInstructions();
                throw ex;
            }
        }
    }

    private static void printTroubleshootingInstructions() {
        System.err.println("\n==================================================================");
        System.err.println("              JDBC CONNECTION TROUBLESHOOTING");
        System.err.println("==================================================================");
        System.err.println("1. Is SQL Server running?");
        System.err.println("   Win+R -> services.msc -> check 'SQL Server (MSSQLSERVER)' or '(SQLEXPRESS)'");
        System.err.println("2. Is TCP/IP enabled?");
        System.err.println("   SQL Server Configuration Manager -> Network Config -> Enable TCP/IP");
        System.err.println("3. Is port 1433 set?");
        System.err.println("   TCP/IP Properties -> IP Addresses -> IPAll -> TCP Port = 1433");
        System.err.println("4. Is SQL Authentication enabled?");
        System.err.println("   Server Properties -> Security -> Mixed Mode");
        System.err.println("   Verify user 'java_user' / 'JavaPassword123!' exists");
        System.err.println("==================================================================\n");
    }
}
