package com.scm.model;

import com.scm.enums.AttendanceStatus;
import jakarta.persistence.*;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Attendance entry.
 */
@Entity
@Table(name = "attendance")
public class Attendance implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(length = 36)
    private String attendanceId;
    
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    @Temporal(TemporalType.DATE)
    @Column
    private Date date;
    
    @Enumerated(EnumType.STRING)
    @Column
    private AttendanceStatus status;
    
    @Column(name = "marked_by", length = 36)
    private String markedBy;
    
    @Column(columnDefinition = "TEXT")
    private String remarks;
    
    @Column(name = "class_type", length = 50)
    private String classType;

    public Attendance() {
    }

    public Attendance(String attendanceId, Student student, Course course,
                      Date date, AttendanceStatus status, String markedBy, String remarks) {
        this.attendanceId = attendanceId;
        this.student = student;
        this.course = course;
        this.date = date == null ? new Date() : date;
        this.status = status;
        this.markedBy = markedBy;
        this.remarks = remarks;
        this.classType = "Lecture";
    }

    public Attendance(String attendanceId, Student student, Course course,
                      AttendanceStatus status, String markedBy, String remarks) {
        this(attendanceId, student, course, new Date(), status, markedBy, remarks);
    }

    public void mark(AttendanceStatus status) {
        this.status = status;
    }

    public void update(AttendanceStatus status) {
        this.status = status;
    }

    public static float getAttendancePercentage(Student student, Course course) {
        List<Attendance> records = student.getAttendanceRecords();
        int total = 0;
        int present = 0;
        for (Attendance att : records) {
            if (att.getCourse().getCourseId().equals(course.getCourseId())) {
                total++;
                if (att.getStatus() == AttendanceStatus.PRESENT || att.getStatus() == AttendanceStatus.LATE) {
                    present++;
                }
            }
        }
        return total > 0 ? (present * 100.0f) / total : 0.0f;
    }

    public String getAttendanceId() {
        return attendanceId;
    }

    public Student getStudent() {
        return student;
    }

    public Course getCourse() {
        return course;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public String getMarkedBy() {
        return markedBy;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getClassType() {
        return classType;
    }

    public void setClassType(String classType) {
        this.classType = classType;
    }

    @Override
    public String toString() {
        return attendanceId + " - " + student.getName() + " - " + status + " - "
                + new SimpleDateFormat("yyyy-MM-dd").format(date);
    }
}
