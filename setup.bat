:: SCM Web - MySQL Quick Setup Script
:: Run this after MySQL is installed and configured

@echo off
echo.
echo ========================================
echo SCM Web - MySQL Setup Script
echo ========================================
echo.
echo Step 1: Creating Database and Tables...
echo Make sure MySQL Server is running!
echo.

:: This script assumes MySQL is installed and root user has no password or password is "root"
:: Modify the password in the connection string if needed

mysql -u root -p root < setup.sql

echo.
echo ========================================
echo Database setup complete!
echo ========================================
echo.
echo Next steps:
echo 1. Make sure Java 17 is installed: java -version
echo 2. Make sure Maven is installed: mvn -version
echo 3. Update application.properties with your MySQL password if it's not "root"
echo 4. Run: mvn clean install
echo 5. Run: mvn spring-boot:run
echo.
echo Access the app at: http://localhost:8080
echo.
pause
