package com.scm.model;

import com.scm.enums.EnrollmentStatus;
import com.scm.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Department head user.
 */
@Entity
@DiscriminatorValue("DEPARTMENT_HEAD")
public class DepartmentHead extends User {
    private static final long serialVersionUID = 1L;

    @Column(name = "department_head_id", length = 20)
    private String departmentHeadId;
    
    @Column(length = 50)
    private String department;
    
    @Column(name = "faculty_id", length = 36)
    private String facultyId;

    public DepartmentHead() {
    }

    public DepartmentHead(String userId, String username, String email, String password,
                          String name, String phoneNumber, String departmentHeadId,
                          String department, String facultyId) {
        super(userId, username, email, password, name, phoneNumber, UserRole.DEPARTMENT_HEAD);
        this.departmentHeadId = departmentHeadId;
        this.department = department;
        this.facultyId = facultyId;
    }

    public boolean approveRegistration(Enrollment enrollment) {
        if (enrollment != null && enrollment.getStatus() == EnrollmentStatus.PENDING) {
            enrollment.approve(this.userId);
            return true;
        }
        return false;
    }

    public boolean rejectRegistration(Enrollment enrollment, String reason) {
        if (enrollment != null && enrollment.getStatus() == EnrollmentStatus.PENDING) {
            enrollment.reject(reason);
            return true;
        }
        return false;
    }

    public List<Grade> reviewGrades(Course course) {
        return course == null ? new ArrayList<>() : course.getAllGrades();
    }

    public boolean approveGrades(List<Grade> grades) {
        for (Grade grade : grades) {
            grade.approve(this.userId, "Approved by Department Head");
        }
        return true;
    }

    public boolean approveSingleGrade(Grade grade) {
        if (grade == null) {
            return false;
        }
        grade.approve(this.userId, "Approved by Department Head");
        return true;
    }

    public boolean rejectGrade(Grade grade, String reason) {
        if (grade == null) {
            return false;
        }
        grade.reject(this.userId, reason);
        return true;
    }

    public Report generateDepartmentReport(String reportType) {
        return new Report(
                "REP" + System.currentTimeMillis(),
                "Department " + reportType,
                this.departmentHeadId,
                "TXT"
        );
    }

    public List<Course> viewDepartmentCourses(List<Course> allCourses) {
        return new ArrayList<>(allCourses);
    }

    public void assignCourseFaculty(Course course, Faculty faculty) {
        course.setFaculty(faculty);
        faculty.addAssignedCourse(course);
    }

    @Override
    public void getDashboard() {
        // not used in web UI
    }

    public String getDepartmentHeadId() {
        return departmentHeadId;
    }

    public String getDepartment() {
        return department;
    }

    public String getFacultyId() {
        return facultyId;
    }
}
