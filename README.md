# Java JDBC SQL Server Database Explorer

A clean, beginner-friendly Java application demonstrating how to connect to Microsoft SQL Server using JDBC (Java Database Connectivity) manually, without Maven. 

It now features both a **Command-Line Explorer** and a **Modern Web-Based Dashboard**!

This project was built to understand database relationships, data seeding, dynamic SQL querying in Java, and serving simple web APIs using Java's built-in HTTP server.

---

## 📂 Project Structure

```text
jdbc_practice/
├── .vscode/
│   ├── settings.json   # Configures VS Code to see the JDBC driver
│   └── launch.json     # 1-click Run & Debug configurations
├── web/
│   └── index.html      # The beautiful front-end Single Page Application (HTML/CSS/JS)
├── DBConnection.java   # Database connection helper with fallback logic
├── SetupDatabase.java  # One-time database setup and sample data seeder
├── Main.java           # Command-Line Database explorer entry point
├── WebServer.java      # Java built-in HTTP server exposing REST APIs for the UI
├── mssql-jdbc.jar      # Microsoft SQL Server JDBC Driver (manual library dependency)
└── README.md           # Instructions on compilation and execution
```

---

## 🚀 Getting Started

### Prerequisites
* **Java Development Kit (JDK)**: Version 8 or higher (like JDK 26).
* **SQL Server**: A local instance running on your machine (Default instance or `SQLEXPRESS`).
* **SQL Server Authentication**: A login user named `java_user` with password `JavaPassword123!` (or configure your own via environment variables).

---

## 🛠️ Installation & Execution

Open your terminal (PowerShell is recommended on Windows) and run the steps below. Alternatively, you can use the built-in "Run & Debug" configurations in VS Code!

### 1. Go to the project directory
```powershell
cd C:\Users\CAIRO\.gemini\antigravity\scratch\jdbc_practice
```

### 2. Compile the Java files
Compile all four Java source files together:
```powershell
& "C:\Program Files\Java\jdk-26.0.1\bin\javac.exe" DBConnection.java SetupDatabase.java Main.java WebServer.java
```

### 3. Run the Database Setup (Run this ONCE)
Before running the applications, run the setup script to create the `CompanyDB` database, tables, and seed it with dummy data. We use the `-cp` flag to include the JDBC driver:
```powershell
& "C:\Program Files\Java\jdk-26.0.1\bin\java.exe" -cp ".;mssql-jdbc.jar" SetupDatabase
```

### 4. Option A: Run the Modern Web Explorer (Recommended)
Launch the built-in HTTP server:
```powershell
& "C:\Program Files\Java\jdk-26.0.1\bin\java.exe" -cp ".;mssql-jdbc.jar" WebServer
```
Then open your browser to **http://localhost:8080**. You will see a beautiful dashboard featuring dynamic grids, insights, and a built-in SQL Console!

### 4. Option B: Run the Command-Line Explorer
If you prefer the terminal, run the CLI main application to execute the queries and see formatted tables in your console output:
```powershell
& "C:\Program Files\Java\jdk-26.0.1\bin\java.exe" -cp ".;mssql-jdbc.jar" Main
```

---

## 🔒 Configuration & Security

To avoid hardcoding sensitive passwords in source code, this application dynamically loads credentials using system environment variables. If these variables are not set, it falls back to local defaults:

| Environment Variable | Description | Default Value |
|----------------------|-------------|---------------|
| `DB_SERVER`          | SQL Server address | `localhost` |
| `DB_PORT`            | TCP/IP Port | `1433` |
| `DB_DATABASE`        | Database Name | `CompanyDB` |
| `DB_USER`            | SQL Login Username | `java_user` |
| `DB_PASSWORD`        | SQL Login Password | `JavaPassword123!` |

### Setting Environment Variables (Optional)
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
