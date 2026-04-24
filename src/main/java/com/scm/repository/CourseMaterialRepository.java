package com.scm.repository;

import com.scm.model.CourseMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseMaterialRepository extends JpaRepository<CourseMaterial, String> {
    @Query("SELECT cm FROM CourseMaterial cm WHERE cm.course.courseId = :courseId")
    List<CourseMaterial> findByCourseId(String courseId);
}
