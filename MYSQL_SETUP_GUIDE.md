# MySQL Setup Guide for Student Course Management System

## Prerequisites
You need to install the following on your laptop:
1. **Java 17** (or higher) - [Download](https://www.oracle.com/java/technologies/downloads/#java17)
2. **MySQL Server** - [Download](https://dev.mysql.com/downloads/mysql/)
3. **Maven** - [Download](https://maven.apache.org/download.cgi)

---

## Step 1: Install MySQL Server

### Windows Installation:
1. Download MySQL installer from https://dev.mysql.com/downloads/mysql/
2. Run the installer and follow the wizard
3. During setup:
   - Choose "Developer Default" or "Server only"
   - Port: 3306 (default)
   - Create a MySQL User (or use root)
   - Remember the password for later

### Verify MySQL Installation:
Open Command Prompt and run:
```bash
mysql --version
```

---

## Step 2: Create the Database

1. **Start MySQL Command Line:**
   ```bash
   mysql -u root -p
   ```
   (Enter your MySQL root password when prompted)

2. **Create the database:**
   ```sql
   CREATE DATABASE scm_db;
   USE scm_db;
   ```

3. **Create the tables:**

   ```sql
   -- Users Table
   CREATE TABLE users (
       id VARCHAR(36) PRIMARY KEY,
       username VARCHAR(100) UNIQUE NOT NULL,
       password VARCHAR(255) NOT NULL,
       email VARCHAR(100),
       full_name VARCHAR(100),
       role VARCHAR(50) NOT NULL,
       is_active BOOLEAN DEFAULT TRUE,
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   );

   -- Courses Table
   CREATE TABLE courses (
       id VARCHAR(36) PRIMARY KEY,
       course_code VARCHAR(50) UNIQUE NOT NULL,
       title VARCHAR(100) NOT NULL,
       description TEXT,
       capacity INT NOT NULL,
       faculty_id VARCHAR(36),
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       FOREIGN KEY (faculty_id) REFERENCES users(id)
   );

   -- Enrollments Table
   CREATE TABLE enrollments (
       id VARCHAR(36) PRIMARY KEY,
       student_id VARCHAR(36) NOT NULL,
       course_id VARCHAR(36) NOT NULL,
       status VARCHAR(50) NOT NULL,
       enrollment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       FOREIGN KEY (student_id) REFERENCES users(id),
       FOREIGN KEY (course_id) REFERENCES courses(id)
   );

   -- Payments Table
   CREATE TABLE payments (
       id VARCHAR(36) PRIMARY KEY,
       student_id VARCHAR(36) NOT NULL,
       course_id VARCHAR(36) NOT NULL,
       amount DECIMAL(10, 2) NOT NULL,
       status VARCHAR(50) NOT NULL,
       payment_date TIMESTAMP,
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       FOREIGN KEY (student_id) REFERENCES users(id),
       FOREIGN KEY (course_id) REFERENCES courses(id)
   );

   -- Attendance Table
   CREATE TABLE attendance (
       id VARCHAR(36) PRIMARY KEY,
       student_id VARCHAR(36) NOT NULL,
       course_id VARCHAR(36) NOT NULL,
       status VARCHAR(50) NOT NULL,
       attendance_date DATE NOT NULL,
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       FOREIGN KEY (student_id) REFERENCES users(id),
       FOREIGN KEY (course_id) REFERENCES courses(id)
   );

   -- Course Materials Table
   CREATE TABLE course_materials (
       id VARCHAR(36) PRIMARY KEY,
       course_id VARCHAR(36) NOT NULL,
       title VARCHAR(100) NOT NULL,
       description TEXT,
       file_path VARCHAR(255),
       uploaded_by VARCHAR(36),
       uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       FOREIGN KEY (course_id) REFERENCES courses(id),
       FOREIGN KEY (uploaded_by) REFERENCES users(id)
   );

   -- Grades Table
   CREATE TABLE grades (
       id VARCHAR(36) PRIMARY KEY,
       student_id VARCHAR(36) NOT NULL,
       course_id VARCHAR(36) NOT NULL,
       grade_value DECIMAL(5, 2),
       status VARCHAR(50),
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       FOREIGN KEY (student_id) REFERENCES users(id),
       FOREIGN KEY (course_id) REFERENCES courses(id)
   );

   -- Notifications Table
   CREATE TABLE notifications (
       id VARCHAR(36) PRIMARY KEY,
       user_id VARCHAR(36) NOT NULL,
       message TEXT,
       is_read BOOLEAN DEFAULT FALSE,
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       FOREIGN KEY (user_id) REFERENCES users(id)
   );
   ```

4. **Verify tables were created:**
   ```sql
   SHOW TABLES;
   ```

5. **Exit MySQL:**
   ```sql
   EXIT;
   ```

---

## Step 3: Configure Project for MySQL

### Update pom.xml

Replace the `<dependencies>` section with:

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>8.0.33</version>
    </dependency>
</dependencies>
```

### Update application.properties

Add these lines to `src/main/resources/application.properties`:

```properties
# MySQL Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/scm_db
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.properties.hibernate.format_sql=true
```

**⚠️ IMPORTANT:** Replace `YOUR_MYSQL_PASSWORD` with the actual MySQL password you set during installation.

---

## Step 4: Build and Run the Project

### Open Command Prompt in the project folder and run:

```bash
# Clean the project
mvn clean

# Build the project
mvn install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

---

## Step 5: Test the Application

1. Open your browser and go to: `http://localhost:8080`
2. You should see the login page
3. Default credentials (add these manually to the database or use the app's signup):
   - Admin / Faculty / Department Head / Student accounts can be created via signup

---

## Troubleshooting

### MySQL Connection Issues:
```bash
# Check if MySQL is running (Windows):
tasklist | findstr mysql

# If not running, start MySQL service:
# Services > MySQL > Start
```

### Maven Issues:
- Make sure Java 17 is installed: `java -version`
- Make sure Maven is in PATH: `mvn -version`

### Port Already in Use:
If port 8080 is already in use, change it in application.properties:
```properties
server.port=9090
```

---

## Database Backup/Restore

### Backup:
```bash
mysqldump -u root -p scm_db > scm_db_backup.sql
```

### Restore:
```bash
mysql -u root -p scm_db < scm_db_backup.sql
```

---

**Enjoy your SCM Web application!**
