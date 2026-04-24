package com.scm.repository;

import com.scm.model.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeRepository extends JpaRepository<Grade, String> {
    @Query("SELECT g FROM Grade g WHERE g.enrollment.student.userId = :studentId")
    List<Grade> findByStudentId(String studentId);
    
    @Query("SELECT g FROM Grade g WHERE g.enrollment.course.courseId = :courseId")
    List<Grade> findByCourseId(String courseId);
    
    @Query("SELECT g FROM Grade g WHERE g.enrollment.student.userId = :studentId AND g.enrollment.course.courseId = :courseId")
    List<Grade> findByStudentIdAndCourseId(String studentId, String courseId);
}
