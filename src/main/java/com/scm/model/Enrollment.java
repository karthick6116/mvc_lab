package com.scm.model;

import com.scm.enums.EnrollmentStatus;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Date;

/**
 * Student course enrollment.
 */
@Entity
@Table(name = "enrollments")
public class Enrollment implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(length = 36)
    private String enrollmentId;
    
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "enrollment_date")
    private Date enrollmentDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status;
    
    @Column(name = "approved_by", length = 36)
    private String approvedBy;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "approval_date")
    private Date approvalDate;
    
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;
    
    @Column
    private int semester;
    
    @Column(name = "academic_year", length = 10)
    private String academicYear;

    public Enrollment() {
    }

    public Enrollment(String enrollmentId, Student student, Course course,
                      int semester, String academicYear) {
        this.enrollmentId = enrollmentId;
        this.student = student;
        this.course = course;
        this.enrollmentDate = new Date();
        this.status = EnrollmentStatus.PENDING;
        this.approvedBy = null;
        this.approvalDate = null;
        this.rejectionReason = null;
        this.semester = semester;
        this.academicYear = academicYear;
    }

    public void approve(String approverUserId) {
        this.approvedBy = approverUserId;
        this.approvalDate = new Date();
        this.rejectionReason = null;
        this.status = EnrollmentStatus.ACTIVE;
    }

    public void reject(String reason) {
        this.rejectionReason = reason;
        this.status = EnrollmentStatus.REJECTED;
    }

    public void drop() {
        this.status = EnrollmentStatus.DROPPED;
    }

    public void complete() {
        this.status = EnrollmentStatus.COMPLETED;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public String getEnrollmentId() {
        return enrollmentId;
    }

    public Student getStudent() {
        return student;
    }

    public Course getCourse() {
        return course;
    }

    public Date getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(Date enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public Date getApprovalDate() {
        return approvalDate;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public int getSemester() {
        return semester;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    @Override
    public String toString() {
        return enrollmentId + " - " + student.getName() + " - " + course.getCourseName() + " - " + status;
    }
}
