package com.scm.model;

import com.scm.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Administrator user.
 */
@Entity
@DiscriminatorValue("ADMINISTRATOR")
public class Administrator extends User {
    private static final long serialVersionUID = 1L;

    @Column(name = "admin_id", length = 20)
    private String adminId;
    
    @Column(length = 50)
    private String department;
    
    @Column(name = "access_level")
    private int accessLevel;

    public Administrator() {
    }

    public Administrator(String userId, String username, String email, String password,
                         String name, String phoneNumber, String adminId, String department) {
        super(userId, username, email, password, name, phoneNumber, UserRole.ADMINISTRATOR);
        this.adminId = adminId;
        this.department = department;
        this.accessLevel = 3;
    }

    public Course createCourse(String courseId, String courseCode, String courseName,
                               String description, int credits, int maxSeats, String department) {
        return new Course(courseId, courseCode, courseName, description, credits, maxSeats, department);
    }

    public void updateCourse(Course course, String courseName, String description, int maxSeats) {
        course.setCourseName(courseName);
        course.setDescription(description);
        course.setMaxSeats(maxSeats);
    }

    public boolean deleteCourse(Course course) {
        return course != null;
    }

    public void assignFacultyToCourse(Faculty faculty, Course course) {
        course.setFaculty(faculty);
        faculty.addAssignedCourse(course);
    }

    public User createUser(User user) {
        return user;
    }

    public boolean deactivateUser(User user) {
        if (user == null) {
            return false;
        }
        user.setActive(false);
        return true;
    }

    public Report generateReport(String reportType) {
        return new Report(
                "REP" + System.currentTimeMillis(),
                reportType,
                this.adminId,
                "TXT"
        );
    }

    public void configureFees(Course course, float amount) {
        course.setFeeAmount(amount);
    }

    public void manageSemesters() {
        // not needed for current web app
    }

    @Override
    public void getDashboard() {
        // not used in web UI
    }

    public String getAdminId() {
        return adminId;
    }

    public String getDepartment() {
        return department;
    }

    public int getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(int accessLevel) {
        this.accessLevel = accessLevel;
    }
}
