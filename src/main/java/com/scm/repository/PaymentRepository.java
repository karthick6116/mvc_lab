package com.scm.repository;

import com.scm.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    @Query("SELECT p FROM Payment p WHERE p.student.userId = :studentId")
    List<Payment> findByStudentId(String studentId);
    
    @Query("SELECT p FROM Payment p WHERE p.student.userId = :studentId AND p.course.courseId = :courseId")
    List<Payment> findByStudentIdAndCourseId(String studentId, String courseId);
}
