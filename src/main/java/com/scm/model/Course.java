package com.scm.model;

import com.scm.enums.EnrollmentStatus;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Academic course.
 */
@Entity
@Table(name = "courses")
public class Course implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(length = 36)
    private String courseId;
    
    @Column(unique = true, nullable = false, length = 50)
    private String courseCode;
    
    @Column(nullable = false, length = 100)
    private String courseName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column
    private int credits;
    
    @Column(name = "max_seats")
    private int maxSeats;
    
    @Column(name = "available_seats")
    private int availableSeats;
    
    @Column(length = 50)
    private String department;
    
    @Column
    private int semester;
    
    @Column(columnDefinition = "TEXT")
    private String prerequisites;
    
    @Column(columnDefinition = "TEXT")
    private String syllabus;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date")
    private Date createdDate;
    
    @Column(name = "fee_amount")
    private float feeAmount;
    
    @ManyToOne
    @JoinColumn(name = "faculty_id")
    private Faculty faculty;
    
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Enrollment> enrollments;
    
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Schedule> schedules;
    
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseMaterial> courseMaterials;

    public Course() {
    }

    public Course(String courseId, String courseCode, String courseName,
                  String description, int credits, int maxSeats, String department) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.description = description == null ? "" : description;
        this.credits = credits;
        this.maxSeats = maxSeats;
        this.availableSeats = maxSeats;
        this.department = department;
        this.semester = 1;
        this.prerequisites = "";
        this.syllabus = "";
        this.createdDate = new Date();
        this.feeAmount = 0.0f;
        this.enrollments = new ArrayList<>();
        this.schedules = new ArrayList<>();
        this.courseMaterials = new ArrayList<>();
    }

    public boolean checkAvailability() {
        return availableSeats > 0;
    }

    public void updateAvailability() {
        int used = 0;
        for (Enrollment enrollment : enrollments) {
            EnrollmentStatus status = enrollment.getStatus();
            if (status == EnrollmentStatus.ACTIVE
                    || status == EnrollmentStatus.APPROVED
                    || status == EnrollmentStatus.COMPLETED) {
                used++;
            }
        }
        this.availableSeats = Math.max(0, maxSeats - used);
    }

    public int getEnrolledCount() {
        updateAvailability();
        return maxSeats - availableSeats;
    }

    public void addPrerequisite(String courseId) {
        if (courseId != null && !courseId.isBlank()) {
            if (prerequisites == null || prerequisites.isEmpty()) {
                prerequisites = courseId;
            } else if (!prerequisites.contains(courseId)) {
                prerequisites = prerequisites + "," + courseId;
            }
        }
    }

    public void removePrerequisite(String courseId) {
        if (prerequisites != null && !prerequisites.isEmpty()) {
            String[] prereqs = prerequisites.split(",");
            prerequisites = String.join(",",
                java.util.Arrays.stream(prereqs)
                    .filter(p -> !p.trim().equals(courseId))
                    .toArray(String[]::new)
            );
        }
    }

    public boolean isFull() {
        return availableSeats <= 0;
    }

    public void addEnrollment(Enrollment enrollment) {
        if (!this.enrollments.contains(enrollment)) {
            this.enrollments.add(enrollment);
            updateAvailability();
        }
    }

    public void removeEnrollment(Enrollment enrollment) {
        this.enrollments.remove(enrollment);
        updateAvailability();
    }

    public void addSchedule(Schedule schedule) {
        this.schedules.add(schedule);
    }

    public void addCourseMaterial(CourseMaterial material) {
        this.courseMaterials.add(material);
    }

    public void removeCourseMaterial(CourseMaterial material) {
        this.courseMaterials.remove(material);
    }

    public List<Grade> getAllGrades() {
        List<Grade> grades = new ArrayList<>();
        // Note: Grades are now retrieved from GradeRepository by enrollment ID
        // This method returns empty list; use GradeRepository.findByEnrollmentId() instead
        return grades;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%d/%d)", courseCode, courseName, getEnrolledCount(), maxSeats);
    }

    public String getCourseId() {
        return courseId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public int getMaxSeats() {
        return maxSeats;
    }

    public void setMaxSeats(int maxSeats) {
        this.maxSeats = maxSeats;
        updateAvailability();
    }

    public int getAvailableSeats() {
        updateAvailability();
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public String getPrerequisites() {
        return prerequisites;
    }

    public void setPrerequisites(String prerequisites) {
        this.prerequisites = prerequisites;
    }

    public String getSyllabus() {
        return syllabus;
    }

    public void setSyllabus(String syllabus) {
        this.syllabus = syllabus;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public float getFeeAmount() {
        return feeAmount;
    }

    public void setFeeAmount(float feeAmount) {
        this.feeAmount = feeAmount;
    }

    public Faculty getFaculty() {
        return faculty;
    }

    public void setFaculty(Faculty faculty) {
        this.faculty = faculty;
    }

    public List<Enrollment> getEnrollments() {
        return enrollments;
    }

    public List<Schedule> getSchedules() {
        return schedules;
    }

    public List<CourseMaterial> getCourseMaterials() {
        return courseMaterials;
    }
}
