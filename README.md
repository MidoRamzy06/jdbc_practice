# Java JDBC SQL Server Database Explorer

A clean, beginner-friendly Java application demonstrating how to connect to Microsoft SQL Server using JDBC (Java Database Connectivity) and Maven.

This project was built as part of an internship exercise to understand database relationships, data seeding, and dynamic SQL querying in Java.

---

## 📂 Project Structure

```text
jdbc_practice/
├── pom.xml                  # Maven configuration and dependencies (mssql-jdbc)
├── .gitignore               # Excludes compiled files and IDE metadata from Git
└── src/
    └── main/
        └── java/
            ├── DBConnection.java   # Database connection helper with fallback logic
            ├── SetupDatabase.java  # One-time database setup and sample data seeder
            └── Main.java           # Database explorer entry point containing SQL queries
```

---

## 🚀 Getting Started

### Prerequisites
* **Java Development Kit (JDK)**: Version 17 or higher.
* **Maven**: For dependency management.
* **SQL Server**: A local instance running on your machine (Default instance or `SQLEXPRESS`).
* **SQL Server Authentication**: A login user named `java_user` with password `JavaPassword123!` (or configure your own via environment variables).

---

## 🛠️ Installation & Execution

### 1. Clone the repository
```bash
git clone https://github.com/your-username/jdbc-practice.git
cd jdbc-practice
```

### 2. Set Up the Database
Before running the explorer, run the setup script to create the `CompanyDB` database, tables, and seed it with dummy data.

**Using your IDE:**
* Open `SetupDatabase.java` and click the **Run** button.

**Using Terminal:**
```bash
mvn compile exec:java -Dexec.mainClass="SetupDatabase"
```

### 3. Run the Database Explorer
Once the database is set up, run the main application to execute the queries and see formatted tables in your console output.

**Using your IDE:**
* Open `Main.java` and click the **Run** button.

**Using Terminal:**
```bash
mvn compile exec:java -Dexec.mainClass="Main"
```

---

## 🔒 Configuration & Security

To avoid hardcoding sensitive passwords in source code, this application dynamically loads credentials using system environment variables. If these variables are not set, it falls back to local development defaults:

| Environment Variable | Description | Default Value |
|----------------------|-------------|---------------|
| `DB_SERVER`          | SQL Server address | `localhost` |
| `DB_PORT`            | TCP/IP Port | `1433` |
| `DB_DATABASE`        | Database Name | `CompanyDB` |
| `DB_USER`            | SQL Login Username | `java_user` |
| `DB_PASSWORD`        | SQL Login Password | `JavaPassword123!` |

### Setting Environment Variables (Optional)
**On Windows (Command Prompt):**
```cmd
set DB_PASSWORD=YourSecurePassword Here
```

**On Windows (PowerShell):**
```powershell
$env:DB_PASSWORD="YourSecurePasswordHere"
```

---

## 📊 Database Schema

The database model represents a standard company workspace:

```text
  [Departments] 1 ─── 👤 ──── * [Employees]
                                  │
                                  *
                          [EmployeeProjects]
                                  *
                                  │
                                  1
                             [Projects]
```

* **Departments**: List of departments with locations and budget.
* **Employees**: Employee directory linked to their corresponding department.
* **Projects**: Project lists with timelines and status tracking.
* **EmployeeProjects**: Many-to-many join table mapping employees to projects with specific roles.
