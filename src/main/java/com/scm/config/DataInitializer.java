package com.scm.config;

import com.scm.model.*;
import com.scm.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Initializes default data into the database on application startup
 */
@Component
public class DataInitializer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeData() {
        // Only initialize if database is empty
        if (userRepository.count() > 0) {
            System.out.println("[INFO] Database already populated with data. Skipping initialization.");
            return;
        }

        System.out.println("[INFO] Initializing default data into database...");

        try {
            // Create users
            Administrator admin = new Administrator("U001", "admin", "admin@university.edu", "admin123",
                    "John Admin", "123-456-7890", "A001", "IT");
            DepartmentHead deptHead = new DepartmentHead("U002", "depthead", "depthead@university.edu", "dept123",
                    "Dr. Sarah Johnson", "123-456-7891", "DH001", "Computer Science", "F001");
            Faculty faculty1 = new Faculty("U003", "faculty1", "faculty1@university.edu", "fac123",
                    "Dr. Michael Smith", "123-456-7892", "F001", "Computer Science", "Associate Professor",
                    "PhD in CS", "AI & Machine Learning", "2015-08-15", "Room 301");
            Faculty faculty2 = new Faculty("U004", "faculty2", "faculty2@university.edu", "fac123",
                    "Dr. Emily Davis", "123-456-7893", "F002", "Computer Science", "Assistant Professor",
                    "PhD in Software Engineering", "Software Development", "2018-01-10", "Room 302");
            Student student1 = new Student("U005", "student1", "student1@university.edu", "stu123",
                    "Alice Brown", "123-456-7894", "S001", 3, 2023, "Computer Science", "2005-03-15", "123 Campus Dr");
            Student student2 = new Student("U006", "student2", "student2@university.edu", "stu123",
                    "Bob Wilson", "123-456-7895", "S002", 3, 2023, "Computer Science", "2004-07-22", "456 College Ave");
            Student student3 = new Student("U007", "student3", "student3@university.edu", "stu123",
                    "Charlie Martinez", "123-456-7896", "S003", 2, 2024, "Computer Science", "2005-11-30", "789 University Blvd");

            userRepository.save(admin);
            userRepository.save(deptHead);
            userRepository.save(faculty1);
            userRepository.save(faculty2);
            userRepository.save(student1);
            userRepository.save(student2);
            userRepository.save(student3);

            // Create courses
            Course c1 = new Course("C001", "CS101", "Introduction to Programming",
                    "Learn fundamentals of programming using Java", 3, 30, "Computer Science");
            c1.setFeeAmount(1500.0f);
            c1.setFaculty(faculty1);
            c1.addSchedule(new Schedule("SCH001", c1, "Monday", "9:00 AM", "10:30 AM", "101", "Building A"));
            c1.addSchedule(new Schedule("SCH002", c1, "Wednesday", "9:00 AM", "10:30 AM", "101", "Building A"));
            courseRepository.save(c1);

            Course c2 = new Course("C002", "CS201", "Data Structures and Algorithms",
                    "Advanced data structures and algorithm analysis", 4, 25, "Computer Science");
            c2.setFeeAmount(1800.0f);
            c2.setFaculty(faculty2);
            c2.addPrerequisite("C001");
            c2.addSchedule(new Schedule("SCH003", c2, "Tuesday", "11:00 AM", "12:30 PM", "201", "Building A"));
            c2.addSchedule(new Schedule("SCH004", c2, "Thursday", "11:00 AM", "12:30 PM", "201", "Building A"));
            courseRepository.save(c2);

            Course c3 = new Course("C003", "CS301", "Database Management Systems",
                    "Design and implementation of database systems", 3, 20, "Computer Science");
            c3.setFeeAmount(1600.0f);
            c3.setFaculty(faculty1);
            c3.addSchedule(new Schedule("SCH005", c3, "Monday", "2:00 PM", "3:30 PM", "102", "Building B"));
            c3.addSchedule(new Schedule("SCH006", c3, "Friday", "2:00 PM", "3:30 PM", "102", "Building B"));
            courseRepository.save(c3);

            Course c4 = new Course("C004", "CS401", "Machine Learning",
                    "Introduction to machine learning algorithms and applications", 4, 15, "Computer Science");
            c4.setFeeAmount(2000.0f);
            c4.setFaculty(faculty1);
            courseRepository.save(c4);

            System.out.println("[INFO] ✅ Default data initialized successfully!");
            System.out.println("[INFO] - 7 users created (1 admin, 1 dept head, 2 faculty, 3 students)");
            System.out.println("[INFO] - 4 courses created with schedules");

        } catch (Exception e) {
            System.err.println("[ERROR] Failed to initialize default data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
