# SCM Web - Quick Setup Instructions

This is a Student Course Management System built with Spring Boot. Follow these steps to set it up on your laptop.

## 📋 Prerequisites

Before starting, make sure you have installed:
- **Java 17+** - [Download](https://www.oracle.com/java/technologies/downloads/#java17)
- **MySQL Server** - [Download](https://dev.mysql.com/downloads/mysql/)
- **Maven** - [Download](https://maven.apache.org/download.cgi)

## 🚀 Quick Setup (5 minutes)

### 1. **Install & Start MySQL**
   - Download and install MySQL Server
   - Start the MySQL service (Services > MySQL > Start)
   - Remember your MySQL password (default: root)

### 2. **Create Database**
   Open Command Prompt/PowerShell and run:
   ```bash
   mysql -u root -p root < setup.sql
   ```
   (Replace `root` with your MySQL password if different)

### 3. **Update Database Password (if needed)**
   Edit `src/main/resources/application.properties`:
   ```properties
   spring.datasource.password=YOUR_MYSQL_PASSWORD
   ```

### 4. **Build & Run**
   In the project folder, open Command Prompt and run:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

### 5. **Access the App**
   Open your browser and go to:
   ```
   http://localhost:8080
   ```

---

## 📚 Detailed Documentation

For more detailed information, see [MYSQL_SETUP_GUIDE.md](MYSQL_SETUP_GUIDE.md)

---

## 🔍 Troubleshooting

### MySQL Connection Error
```
ERROR 1045 (28000): Access denied for user 'root'@'localhost'
```
**Solution:** Check your MySQL password in `application.properties`

### Maven Build Fails
```bash
mvn -v  # Check if Maven is installed
java -version  # Check if Java 17 is installed
```

### Port 8080 Already in Use
Edit `application.properties`:
```properties
server.port=9090
```

### MySQL Service Not Running
- Windows: Services > MySQL > Start
- Or use: `net start MySQL80`

---

## 📋 Project Structure

```
src/main/
├── java/com/scm/
│   ├── ScmApplication.java
│   ├── controller/        # Web controllers
│   ├── model/            # Database models
│   ├── enums/            # Enumerations
│   └── util/             # Utilities
└── resources/
    ├── application.properties
    ├── static/           # CSS, JS
    └── templates/        # HTML pages
```

---

## 👥 Default Roles

The system supports 4 user roles:
- **Student** - View courses, grades, attendance
- **Faculty** - Manage attendance, grades, materials
- **Department Head** - Approve enrollments, view reports
- **Admin** - Manage users, courses, system settings

Create accounts via the signup page or insert directly into the database.

---

## 💾 Database Schema

The database includes tables for:
- Users (students, faculty, dept heads, admins)
- Courses
- Enrollments
- Payments
- Attendance
- Grades
- Course Materials
- Notifications
- Schedules
- Reports

---

## 📞 Need Help?

1. Check [MYSQL_SETUP_GUIDE.md](MYSQL_SETUP_GUIDE.md) for detailed steps
2. Check application logs for errors
3. Verify MySQL is running and accessible
4. Ensure Java 17+ and Maven are installed

---

**Happy coding! 🎉**
