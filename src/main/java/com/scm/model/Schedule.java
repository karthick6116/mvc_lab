package com.scm.model;

import jakarta.persistence.*;

import java.io.Serializable;

/**
 * Schedule entry.
 */
@Entity
@Table(name = "schedules")
public class Schedule implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(length = 36)
    private String scheduleId;
    
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    @Column(name = "day_of_week", length = 20)
    private String dayOfWeek;
    
    @Column(name = "start_time", length = 10)
    private String startTime;
    
    @Column(name = "end_time", length = 10)
    private String endTime;
    
    @Column(length = 50)
    private String room;
    
    @Column(length = 50)
    private String building;
    
    @Column(name = "schedule_type", length = 50)
    private String scheduleType;

    public Schedule() {
    }

    public Schedule(String scheduleId, Course course, String dayOfWeek,
                    String startTime, String endTime, String room, String building) {
        this.scheduleId = scheduleId;
        this.course = course;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
        this.building = building;
        this.scheduleType = "Lecture";
    }

    public boolean checkConflict(Schedule other) {
        return other != null
                && this.dayOfWeek.equalsIgnoreCase(other.dayOfWeek)
                && this.startTime.equalsIgnoreCase(other.startTime);
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public Course getCourse() {
        return course;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getRoom() {
        return room;
    }

    public String getBuilding() {
        return building;
    }

    public String getScheduleType() {
        return scheduleType;
    }
}
