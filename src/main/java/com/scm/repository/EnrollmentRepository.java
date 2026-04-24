package com.scm.repository;

import com.scm.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, String> {
    @Query("SELECT e FROM Enrollment e WHERE e.student.userId = :studentId")
    List<Enrollment> findByStudentId(String studentId);
    
    @Query("SELECT e FROM Enrollment e WHERE e.course.courseId = :courseId")
    List<Enrollment> findByCourseId(String courseId);
    
    @Query("SELECT e FROM Enrollment e WHERE e.student.userId = :studentId AND e.course.courseId = :courseId")
    List<Enrollment> findByStudentIdAndCourseId(String studentId, String courseId);
}
