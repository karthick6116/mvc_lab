package com.scm.model;

import com.scm.enums.UserRole;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Student user.
 */
@Entity
@DiscriminatorValue("STUDENT")
public class Student extends User {
    private static final long serialVersionUID = 1L;

    @Column(name = "student_id", length = 20)
    private String studentId;
    
    @Column
    private int semester;
    
    @Column(name = "cgpa")
    private float cgpa;
    
    @Column(name = "admission_year")
    private int admissionYear;
    
    @Column(length = 50)
    private String major;
    
    @Column(name = "date_of_birth", length = 20)
    private String dateOfBirth;
    
    @Column(columnDefinition = "TEXT")
    private String address;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Enrollment> enrollments;
    
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments;
    
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attendance> attendanceRecords;

    public Student() {
    }

    public Student(String userId, String username, String email, String password,
                   String name, String phoneNumber, String studentId, int semester,
                   int admissionYear, String major, String dateOfBirth, String address) {
        super(userId, username, email, password, name, phoneNumber, UserRole.STUDENT);
        this.studentId = studentId;
        this.semester = semester;
        this.cgpa = 0.0f;
        this.admissionYear = admissionYear;
        this.major = major;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.enrollments = new ArrayList<>();
        this.payments = new ArrayList<>();
        this.attendanceRecords = new ArrayList<>();
    }

    public List<Course> browseCourses(List<Course> allCourses) {
        return new ArrayList<>(allCourses);
    }

    public List<Course> searchCourses(List<Course> allCourses, String keyword) {
        List<Course> results = new ArrayList<>();
        if (keyword == null) {
            return results;
        }
        String q = keyword.trim().toLowerCase();
        for (Course course : allCourses) {
            if (course.getCourseName().toLowerCase().contains(q)
                    || course.getCourseCode().toLowerCase().contains(q)
                    || course.getDepartment().toLowerCase().contains(q)) {
                results.add(course);
            }
        }
        return results;
    }

    public boolean registerForCourse(Course course) {
        return course != null;
    }

    public boolean dropCourse(Enrollment enrollment) {
        return enrollment != null;
    }

    public List<Schedule> viewSchedule() {
        List<Schedule> schedules = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getStatus() == com.scm.enums.EnrollmentStatus.ACTIVE
                    || enrollment.getStatus() == com.scm.enums.EnrollmentStatus.COMPLETED
                    || enrollment.getStatus() == com.scm.enums.EnrollmentStatus.APPROVED) {
                schedules.addAll(enrollment.getCourse().getSchedules());
            }
        }
        return schedules;
    }

    public List<Grade> viewGrades() {
        List<Grade> grades = new ArrayList<>();
        // Note: Grades are now retrieved from GradeRepository by enrollment ID
        // This method returns empty list; use GradeRepository.findByStudentId() instead
        return grades;
    }

    public List<Attendance> viewAttendance() {
        return new ArrayList<>(attendanceRecords);
    }

    public List<Course> getEnrolledCourses() {
        List<Course> courses = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getStatus() == com.scm.enums.EnrollmentStatus.ACTIVE
                    || enrollment.getStatus() == com.scm.enums.EnrollmentStatus.COMPLETED
                    || enrollment.getStatus() == com.scm.enums.EnrollmentStatus.APPROVED) {
                courses.add(enrollment.getCourse());
            }
        }
        return courses;
    }

    public void calculateCGPA() {
        float total = 0.0f;
        int count = 0;
        for (Enrollment enrollment : enrollments) {
            // Note: Grades are now retrieved from GradeRepository
            // This calculation would need to be done via repository instead
        }
        this.cgpa = count == 0 ? 0.0f : total / count;
    }

    @Override
    public void getDashboard() {
        // not used in web UI
    }

    public String getStudentId() {
        return studentId;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public float getCgpa() {
        calculateCGPA();
        return cgpa;
    }

    public void setCgpa(float cgpa) {
        this.cgpa = cgpa;
    }

    public int getAdmissionYear() {
        return admissionYear;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<Enrollment> getEnrollments() {
        return enrollments;
    }

    public void setEnrollments(List<Enrollment> enrollments) {
        this.enrollments = enrollments;
    }

    public void addEnrollment(Enrollment enrollment) {
        if (!this.enrollments.contains(enrollment)) {
            this.enrollments.add(enrollment);
        }
    }

    public void removeEnrollment(Enrollment enrollment) {
        this.enrollments.remove(enrollment);
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public void addPayment(Payment payment) {
        if (!this.payments.contains(payment)) {
            this.payments.add(payment);
        }
    }

    public List<Attendance> getAttendanceRecords() {
        return attendanceRecords;
    }

    public void addAttendanceRecord(Attendance attendance) {
        if (!this.attendanceRecords.contains(attendance)) {
            this.attendanceRecords.add(attendance);
        }
    }

    public void replaceAttendanceRecord(Attendance attendance) {
        this.attendanceRecords.removeIf(existing -> existing.getAttendanceId().equals(attendance.getAttendanceId()));
        this.attendanceRecords.add(attendance);
    }
}
