package com.scm.repository;

import com.scm.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, String> {
    @Query("SELECT a FROM Attendance a WHERE a.student.userId = :studentId")
    List<Attendance> findByStudentId(String studentId);
    
    @Query("SELECT a FROM Attendance a WHERE a.student.userId = :studentId AND a.course.courseId = :courseId")
    List<Attendance> findByStudentIdAndCourseId(String studentId, String courseId);
    
    @Query("SELECT a FROM Attendance a WHERE a.course.courseId = :courseId")
    List<Attendance> findByCourseId(String courseId);
}
