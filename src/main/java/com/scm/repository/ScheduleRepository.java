package com.scm.repository;

import com.scm.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, String> {
    @Query("SELECT s FROM Schedule s WHERE s.course.courseId = :courseId")
    List<Schedule> findByCourseId(String courseId);
}
