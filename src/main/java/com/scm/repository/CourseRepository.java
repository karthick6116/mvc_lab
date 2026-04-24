package com.scm.repository;

import com.scm.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, String> {
    Optional<Course> findByCourseCode(String courseCode);
    
    @Query("SELECT c FROM Course c WHERE c.faculty.userId = :facultyId")
    List<Course> findByFacultyId(String facultyId);
}
