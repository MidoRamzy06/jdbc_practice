import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== CompanyDB Explorer ===\n");

        try (Connection connection = DBConnection.getConnection()) {
            System.out.println("Connection successful!\n");

            // 1. List all tables in CompanyDB
            System.out.println("--- Tables in CompanyDB ---");
            query(connection,
                "SELECT TABLE_NAME, TABLE_TYPE " +
                "FROM INFORMATION_SCHEMA.TABLES " +
                "WHERE TABLE_TYPE = 'BASE TABLE' " +
                "ORDER BY TABLE_NAME"
            );

            // 2. Show all departments
            System.out.println("\n--- All Departments ---");
            query(connection, "SELECT * FROM Departments");

            // 3. Show all employees with their department names
            System.out.println("\n--- All Employees (with Department) ---");
            query(connection,
                "SELECT e.employee_id, e.first_name, e.last_name, " +
                "       e.email, e.hire_date, e.salary, d.name AS department " +
                "FROM Employees e " +
                "JOIN Departments d ON e.department_id = d.department_id " +
                "ORDER BY e.employee_id"
            );

            // 4. Show all projects and their status
            System.out.println("\n--- All Projects ---");
            query(connection, "SELECT * FROM Projects");

            // 5. Show who is assigned to which project
            System.out.println("\n--- Project Assignments ---");
            query(connection,
                "SELECT e.first_name + ' ' + e.last_name AS employee, " +
                "       p.name AS project, ep.role " +
                "FROM EmployeeProjects ep " +
                "JOIN Employees e ON ep.employee_id = e.employee_id " +
                "JOIN Projects p ON ep.project_id = p.project_id " +
                "ORDER BY p.name, ep.role"
            );

            // 6. Show average salary per department
            System.out.println("\n--- Average Salary by Department ---");
            query(connection,
                "SELECT d.name AS department, " +
                "       COUNT(e.employee_id) AS employee_count, " +
                "       FORMAT(AVG(e.salary), 'N2') AS avg_salary, " +
                "       FORMAT(SUM(e.salary), 'N2') AS total_salary " +
                "FROM Departments d " +
                "LEFT JOIN Employees e ON d.department_id = e.department_id " +
                "GROUP BY d.name " +
                "ORDER BY AVG(e.salary) DESC"
            );

            // 7. Row counts for each table
            System.out.println("\n--- Row Counts ---");
            query(connection,
                "SELECT t.name AS [table], p.rows AS row_count " +
                "FROM sys.tables t " +
                "JOIN sys.partitions p ON t.object_id = p.object_id AND p.index_id IN (0,1) " +
                "ORDER BY t.name"
            );

        } catch (Exception e) {
            System.err.println("Database connection failed!");
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * Runs any SQL query and prints all rows/columns in a formatted table.
     */
    private static void query(Connection connection, String sql) {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            int columnCount = rs.getMetaData().getColumnCount();

            // Print column headers
            for (int i = 1; i <= columnCount; i++) {
                if (i > 1) System.out.print("  |  ");
                System.out.print(rs.getMetaData().getColumnLabel(i));
            }
            System.out.println();
            System.out.println("-".repeat(80));

            // Print rows
            int rowCount = 0;
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    if (i > 1) System.out.print("  |  ");
                    String value = rs.getString(i);
                    System.out.print(value != null ? value : "NULL");
                }
                System.out.println();
                rowCount++;
            }

            if (rowCount == 0) {
                System.out.println("(no results)");
            }
            System.out.println();

        } catch (Exception e) {
            System.err.println("Query failed: " + e.getMessage());
        }
    }
}
