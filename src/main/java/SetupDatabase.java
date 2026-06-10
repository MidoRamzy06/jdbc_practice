import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Run this ONCE to create the CompanyDB database with sample tables and data.
 * After running, you can switch to Main.java to explore the data.
 */
public class SetupDatabase {
    public static void main(String[] args) {
        System.out.println("=== Database Setup Script ===\n");

        // Step 1: Connect to 'master' first (we need it to CREATE a new database)
        String masterUrl = "jdbc:sqlserver://localhost:1433;databaseName=master;"
                         + "encrypt=true;trustServerCertificate=true;loginTimeout=5;";

        try (Connection connection = DriverManager.getConnection(masterUrl, "java_user", "JavaPassword123!")) {
            Statement stmt = connection.createStatement();

            // Step 2: Create the CompanyDB database (if it doesn't exist)
            System.out.println("Creating database CompanyDB...");
            stmt.execute(
                "IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'CompanyDB') " +
                "CREATE DATABASE CompanyDB"
            );
            System.out.println("Database ready.\n");

            // Step 3: Switch to the new database
            stmt.execute("USE CompanyDB");

            // Step 4: Create tables
            System.out.println("Creating tables...");

            // Departments table
            stmt.execute(
                "IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Departments') " +
                "CREATE TABLE Departments (" +
                "   department_id   INT PRIMARY KEY IDENTITY(1,1), " +
                "   name            VARCHAR(100) NOT NULL, " +
                "   location        VARCHAR(100), " +
                "   budget          DECIMAL(12,2)" +
                ")"
            );
            System.out.println("  - Departments table created");

            // Employees table
            stmt.execute(
                "IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Employees') " +
                "CREATE TABLE Employees (" +
                "   employee_id     INT PRIMARY KEY IDENTITY(1,1), " +
                "   first_name      VARCHAR(50) NOT NULL, " +
                "   last_name       VARCHAR(50) NOT NULL, " +
                "   email           VARCHAR(100), " +
                "   hire_date       DATE, " +
                "   salary          DECIMAL(10,2), " +
                "   department_id   INT, " +
                "   FOREIGN KEY (department_id) REFERENCES Departments(department_id)" +
                ")"
            );
            System.out.println("  - Employees table created");

            // Projects table
            stmt.execute(
                "IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Projects') " +
                "CREATE TABLE Projects (" +
                "   project_id      INT PRIMARY KEY IDENTITY(1,1), " +
                "   name            VARCHAR(100) NOT NULL, " +
                "   start_date      DATE, " +
                "   end_date        DATE, " +
                "   status          VARCHAR(20) DEFAULT 'Active'" +
                ")"
            );
            System.out.println("  - Projects table created");

            // EmployeeProjects (many-to-many relationship)
            stmt.execute(
                "IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'EmployeeProjects') " +
                "CREATE TABLE EmployeeProjects (" +
                "   employee_id     INT NOT NULL, " +
                "   project_id      INT NOT NULL, " +
                "   role            VARCHAR(50), " +
                "   PRIMARY KEY (employee_id, project_id), " +
                "   FOREIGN KEY (employee_id) REFERENCES Employees(employee_id), " +
                "   FOREIGN KEY (project_id) REFERENCES Projects(project_id)" +
                ")"
            );
            System.out.println("  - EmployeeProjects table created");

            // Step 5: Insert sample data (only if tables are empty)
            System.out.println("\nInserting sample data...");

            // Check if data already exists
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM Departments");
            rs.next();
            if (rs.getInt(1) > 0) {
                System.out.println("  Data already exists — skipping inserts.");
            } else {
                // Insert departments
                stmt.execute(
                    "INSERT INTO Departments (name, location, budget) VALUES " +
                    "('Engineering',  'Building A - Floor 3', 500000.00), " +
                    "('Marketing',    'Building B - Floor 1', 300000.00), " +
                    "('Human Resources', 'Building A - Floor 1', 200000.00), " +
                    "('Finance',      'Building C - Floor 2', 350000.00), " +
                    "('Operations',   'Building B - Floor 2', 250000.00)"
                );
                System.out.println("  - 5 departments inserted");

                // Insert employees
                stmt.execute(
                    "INSERT INTO Employees (first_name, last_name, email, hire_date, salary, department_id) VALUES " +
                    "('Ahmed',   'Hassan',    'ahmed.hassan@company.com',    '2023-01-15', 85000.00, 1), " +
                    "('Sara',    'Ali',       'sara.ali@company.com',        '2023-03-20', 78000.00, 1), " +
                    "('Omar',    'Khalil',    'omar.khalil@company.com',     '2022-06-01', 92000.00, 1), " +
                    "('Nour',    'Ibrahim',   'nour.ibrahim@company.com',    '2023-07-10', 65000.00, 2), " +
                    "('Youssef', 'Mohamed',   'youssef.mohamed@company.com', '2022-11-05', 70000.00, 2), " +
                    "('Layla',   'Farouk',    'layla.farouk@company.com',    '2024-01-08', 60000.00, 3), " +
                    "('Karim',   'Mostafa',   'karim.mostafa@company.com',   '2021-09-15', 95000.00, 4), " +
                    "('Dina',    'Saeed',     'dina.saeed@company.com',      '2023-05-22', 72000.00, 4), " +
                    "('Tarek',   'Nabil',     'tarek.nabil@company.com',     '2022-02-14', 68000.00, 5), " +
                    "('Mariam',  'Ashraf',    'mariam.ashraf@company.com',   '2024-03-01', 62000.00, 5)"
                );
                System.out.println("  - 10 employees inserted");

                // Insert projects
                stmt.execute(
                    "INSERT INTO Projects (name, start_date, end_date, status) VALUES " +
                    "('Website Redesign',    '2024-01-01', '2024-06-30', 'Completed'), " +
                    "('Mobile App v2',       '2024-03-15', '2024-12-31', 'Active'), " +
                    "('Data Migration',      '2024-06-01', '2024-09-30', 'Active'), " +
                    "('Marketing Campaign',  '2024-02-01', '2024-05-31', 'Completed'), " +
                    "('ERP Integration',     '2024-07-01', NULL,         'Active')"
                );
                System.out.println("  - 5 projects inserted");

                // Assign employees to projects
                stmt.execute(
                    "INSERT INTO EmployeeProjects (employee_id, project_id, role) VALUES " +
                    "(1, 1, 'Lead Developer'), " +
                    "(2, 1, 'Frontend Dev'), " +
                    "(3, 2, 'Project Manager'), " +
                    "(1, 2, 'Backend Dev'), " +
                    "(4, 4, 'Campaign Lead'), " +
                    "(5, 4, 'Content Creator'), " +
                    "(7, 3, 'Data Analyst'), " +
                    "(8, 3, 'Finance Review'), " +
                    "(9, 5, 'Operations Lead'), " +
                    "(3, 5, 'Tech Lead')"
                );
                System.out.println("  - 10 project assignments inserted");
            }

            System.out.println("\n=== Setup Complete! ===");
            System.out.println("Now run Main.java to explore the data.");

        } catch (SQLException e) {
            System.err.println("Setup failed: " + e.getMessage());
        }
    }
}
