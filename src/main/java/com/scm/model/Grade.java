package com.scm.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Date;

/**
 * Grade with approval workflow.
 */
@Entity
@Table(name = "grades")
public class Grade implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(length = 36)
    private String gradeId;
    
    @ManyToOne
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;
    
    @Column
    private float marks;
    
    @Column(name = "total_marks")
    private float totalMarks;
    
    @Column(length = 5)
    private String grade;
    
    @Column
    private float gpa;
    
    @Column(name = "exam_type", length = 50)
    private String examType;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "submitted_date")
    private Date submittedDate;
    
    @Column(name = "reviewed_by", length = 36)
    private String reviewedBy;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "review_date")
    private Date reviewDate;
    
    @Column(name = "is_approved")
    private boolean isApproved;
    
    @Column(columnDefinition = "TEXT")
    private String comments;

    public Grade() {
    }

    public Grade(String gradeId, Enrollment enrollment, float marks,
                 float totalMarks, String examType) {
        this.gradeId = gradeId;
        this.enrollment = enrollment;
        this.marks = marks;
        this.totalMarks = totalMarks;
        this.examType = examType;
        this.submittedDate = new Date();
        this.reviewedBy = null;
        this.reviewDate = null;
        this.isApproved = false;
        this.comments = "Awaiting department head review";
        this.grade = calculateGrade();
        this.gpa = calculateGPA();
    }

    public String calculateGrade() {
        if (totalMarks <= 0) {
            return "F";
        }
        float percentage = (marks / totalMarks) * 100.0f;
        if (percentage >= 90) return "A+";
        if (percentage >= 85) return "A";
        if (percentage >= 80) return "A-";
        if (percentage >= 75) return "B+";
        if (percentage >= 70) return "B";
        if (percentage >= 65) return "B-";
        if (percentage >= 60) return "C+";
        if (percentage >= 55) return "C";
        if (percentage >= 50) return "C-";
        if (percentage >= 45) return "D";
        return "F";
    }

    public float calculateGPA() {
        switch (grade) {
            case "A+":
            case "A":
                return 4.0f;
            case "A-":
                return 3.7f;
            case "B+":
                return 3.3f;
            case "B":
                return 3.0f;
            case "B-":
                return 2.7f;
            case "C+":
                return 2.3f;
            case "C":
                return 2.0f;
            case "C-":
                return 1.7f;
            case "D":
                return 1.0f;
            default:
                return 0.0f;
        }
    }

    public void submitForReview() {
        this.isApproved = false;
        this.comments = "Awaiting department head review";
        this.reviewedBy = null;
        this.reviewDate = null;
    }

    public void approve() {
        approve(null, "Approved");
    }

    public void approve(String reviewer, String comments) {
        this.isApproved = true;
        this.reviewedBy = reviewer;
        this.reviewDate = new Date();
        this.comments = comments == null ? "Approved" : comments;
    }

    public void reject(String reason) {
        reject(null, reason);
    }

    public void reject(String reviewer, String reason) {
        this.isApproved = false;
        this.reviewedBy = reviewer;
        this.reviewDate = new Date();
        this.comments = reason == null || reason.isBlank() ? "Needs correction" : reason;
    }

    public String getGradeId() {
        return gradeId;
    }

    public Enrollment getEnrollment() {
        return enrollment;
    }

    public float getMarks() {
        return marks;
    }

    public void setMarks(float marks) {
        this.marks = marks;
        this.grade = calculateGrade();
        this.gpa = calculateGPA();
    }

    public float getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(float totalMarks) {
        this.totalMarks = totalMarks;
        this.grade = calculateGrade();
        this.gpa = calculateGPA();
    }

    public String getGrade() {
        return grade;
    }

    public float getGpa() {
        return gpa;
    }

    public String getExamType() {
        return examType;
    }

    public Date getSubmittedDate() {
        return submittedDate;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public Date getReviewDate() {
        return reviewDate;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    @Override
    public String toString() {
        return gradeId + " - " + grade + " - " + (isApproved ? "Approved" : "Pending");
    }
}
