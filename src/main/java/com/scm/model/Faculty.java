package com.scm.model;

import com.scm.enums.AttendanceStatus;
import com.scm.enums.UserRole;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Faculty user.
 */
@Entity
@DiscriminatorValue("FACULTY")
public class Faculty extends User {
    private static final long serialVersionUID = 1L;

    @Column(name = "faculty_id", length = 20)
    private String facultyId;
    
    @Column(length = 50)
    private String department;
    
    @Column(length = 50)
    private String designation;
    
    @Column(length = 100)
    private String qualification;
    
    @Column(length = 100)
    private String specialization;
    
    @Column(name = "joining_date", length = 20)
    private String joiningDate;
    
    @Column(name = "office_room", length = 50)
    private String officeRoom;
    
    @OneToMany(mappedBy = "faculty", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Course> assignedCourses;

    public Faculty() {
    }

    public Faculty(String userId, String username, String email, String password,
                   String name, String phoneNumber, String facultyId, String department,
                   String designation, String qualification, String specialization,
                   String joiningDate, String officeRoom) {
        super(userId, username, email, password, name, phoneNumber, UserRole.FACULTY);
        this.facultyId = facultyId;
        this.department = department;
        this.designation = designation;
        this.qualification = qualification;
        this.specialization = specialization;
        this.joiningDate = joiningDate;
        this.officeRoom = officeRoom;
        this.assignedCourses = new ArrayList<>();
    }

    public List<Course> viewAssignedCourses() {
        return new ArrayList<>(assignedCourses);
    }

    public CourseMaterial uploadCourseMaterial(Course course, String title, String description,
                                               String fileType, String filePath) {
        CourseMaterial material = new CourseMaterial(
                "MAT" + System.currentTimeMillis(),
                title,
                description,
                fileType,
                filePath,
                course
        );
        course.addCourseMaterial(material);
        return material;
    }

    public Attendance recordAttendance(Course course, Student student, AttendanceStatus status,
                                       String remarks, Date date) {
        Attendance attendance = new Attendance(
                "ATT" + System.currentTimeMillis(),
                student,
                course,
                date,
                status,
                this.facultyId,
                remarks
        );
        student.replaceAttendanceRecord(attendance);
        return attendance;
    }

    public Grade submitGrades(Enrollment enrollment, float marks, float totalMarks, String examType) {
        Grade grade = new Grade(
                "GRD" + System.currentTimeMillis(),
                enrollment,
                marks,
                totalMarks,
                examType
        );
        // Grade now manages the relationship; no need to set it on enrollment
        return grade;
    }

    public List<Student> viewEnrolledStudents(Course course) {
        List<Student> students = new ArrayList<>();
        for (Enrollment enrollment : course.getEnrollments()) {
            if (enrollment.getStatus() == com.scm.enums.EnrollmentStatus.ACTIVE
                    || enrollment.getStatus() == com.scm.enums.EnrollmentStatus.COMPLETED
                    || enrollment.getStatus() == com.scm.enums.EnrollmentStatus.APPROVED) {
                students.add(enrollment.getStudent());
            }
        }
        return students;
    }

    public Report generateCourseReport(Course course) {
        return new Report(
                "REP" + System.currentTimeMillis(),
                "Course Report",
                this.facultyId,
                "TXT"
        );
    }

    public void updateSyllabus(Course course, String syllabus) {
        course.setSyllabus(syllabus);
    }

    @Override
    public void getDashboard() {
        // not used in web UI
    }

    public String getFacultyId() {
        return facultyId;
    }

    public String getDepartment() {
        return department;
    }

    public String getDesignation() {
        return designation;
    }

    public String getQualification() {
        return qualification;
    }

    public String getSpecialization() {
        return specialization;
    }

    public String getJoiningDate() {
        return joiningDate;
    }

    public String getOfficeRoom() {
        return officeRoom;
    }

    public List<Course> getAssignedCourses() {
        return assignedCourses;
    }

    public void addAssignedCourse(Course course) {
        if (!this.assignedCourses.contains(course)) {
            this.assignedCourses.add(course);
        }
    }

    public void removeAssignedCourse(Course course) {
        this.assignedCourses.remove(course);
    }
}
