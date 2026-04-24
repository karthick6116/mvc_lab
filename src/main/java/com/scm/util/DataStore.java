package com.scm.util;

import com.scm.enums.AttendanceStatus;
import com.scm.enums.EnrollmentStatus;
import com.scm.enums.PaymentStatus;
import com.scm.model.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * File-backed repository for the web app.
 * Uses Java serialization so the app works locally without an external database.
 */
public class DataStore {

    public static class OperationResult<T> {
        private final boolean success;
        private final String message;
        private final T data;

        public OperationResult(boolean success, String message, T data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public T getData() {
            return data;
        }

        public static <T> OperationResult<T> ok(String message, T data) {
            return new OperationResult<>(true, message, data);
        }

        public static <T> OperationResult<T> fail(String message) {
            return new OperationResult<>(false, message, null);
        }
    }

    private static class DatabaseState implements Serializable {
        private static final long serialVersionUID = 1L;
        private Map<String, User> users = new LinkedHashMap<>();
        private Map<String, Course> courses = new LinkedHashMap<>();
        private Map<String, Enrollment> enrollments = new LinkedHashMap<>();
        private Map<String, Payment> payments = new LinkedHashMap<>();
        private Map<String, Attendance> attendance = new LinkedHashMap<>();
        private Map<String, CourseMaterial> materials = new LinkedHashMap<>();
        private Map<String, Notification> notifications = new LinkedHashMap<>();
    }

    private static final Object LOCK = new Object();
    private static final Path DATA_DIR = Paths.get("scm_data");
    private static final Path DATA_FILE = DATA_DIR.resolve("scm-data.ser");
    private static final Path MATERIALS_DIR = DATA_DIR.resolve("materials");
    private static DatabaseState state;
    private static boolean initialized = false;

    public static void initializeSampleData() {
        synchronized (LOCK) {
            if (initialized) {
                return;
            }
            try {
                Files.createDirectories(DATA_DIR);
                Files.createDirectories(MATERIALS_DIR);
                if (Files.exists(DATA_FILE)) {
                    loadState();
                }
                if (state == null || state.users.isEmpty()) {
                    state = new DatabaseState();
                    seedDefaultData();
                    saveState();
                } else {
                    reconcileState();
                    saveState();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                state = new DatabaseState();
                seedDefaultData();
                saveState();
            }
            initialized = true;
        }
    }

    private static void ensureInitialized() {
        if (!initialized) {
            initializeSampleData();
        }
    }

    private static void loadState() throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(DATA_FILE))) {
            state = (DatabaseState) in.readObject();
        }
    }

    private static void saveState() {
        try {
            Files.createDirectories(DATA_DIR);
            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(DATA_FILE))) {
                out.writeObject(state);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to persist SCM data", e);
        }
    }

    private static String id(String prefix) {
        return prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase(Locale.ROOT);
    }

    public static Path getMaterialsDir() {
        ensureInitialized();
        return MATERIALS_DIR;
    }

    private static void seedDefaultData() {
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

        addUserInternal(admin);
        addUserInternal(deptHead);
        addUserInternal(faculty1);
        addUserInternal(faculty2);
        addUserInternal(student1);
        addUserInternal(student2);
        addUserInternal(student3);

        Course c1 = new Course("C001", "CS101", "Introduction to Programming",
                "Learn fundamentals of programming using Java", 3, 30, "Computer Science");
        c1.setFeeAmount(1500.0f);
        c1.setFaculty(faculty1);
        faculty1.addAssignedCourse(c1);
        c1.addSchedule(new Schedule("SCH001", c1, "Monday", "9:00 AM", "10:30 AM", "101", "Building A"));
        c1.addSchedule(new Schedule("SCH002", c1, "Wednesday", "9:00 AM", "10:30 AM", "101", "Building A"));

        Course c2 = new Course("C002", "CS201", "Data Structures and Algorithms",
                "Advanced data structures and algorithm analysis", 4, 25, "Computer Science");
        c2.setFeeAmount(1800.0f);
        c2.setFaculty(faculty2);
        faculty2.addAssignedCourse(c2);
        c2.addPrerequisite("C001");
        c2.addSchedule(new Schedule("SCH003", c2, "Tuesday", "11:00 AM", "12:30 PM", "201", "Building A"));
        c2.addSchedule(new Schedule("SCH004", c2, "Thursday", "11:00 AM", "12:30 PM", "201", "Building A"));

        Course c3 = new Course("C003", "CS301", "Database Management Systems",
                "Design and implementation of database systems", 3, 20, "Computer Science");
        c3.setFeeAmount(1600.0f);
        c3.setFaculty(faculty1);
        faculty1.addAssignedCourse(c3);
        c3.addSchedule(new Schedule("SCH005", c3, "Monday", "2:00 PM", "3:30 PM", "102", "Building B"));
        c3.addSchedule(new Schedule("SCH006", c3, "Friday", "2:00 PM", "3:30 PM", "102", "Building B"));

        Course c4 = new Course("C004", "CS401", "Machine Learning",
                "Introduction to machine learning algorithms and applications", 4, 15, "Computer Science");
        c4.setFeeAmount(2000.0f);
        c4.setFaculty(faculty1);
        faculty1.addAssignedCourse(c4);
        c4.addPrerequisite("C002");

        addCourseInternal(c1);
        addCourseInternal(c2);
        addCourseInternal(c3);
        addCourseInternal(c4);
    }

    private static void reconcileState() {
        if (state.users == null) state.users = new LinkedHashMap<>();
        if (state.courses == null) state.courses = new LinkedHashMap<>();
        if (state.enrollments == null) state.enrollments = new LinkedHashMap<>();
        if (state.payments == null) state.payments = new LinkedHashMap<>();
        if (state.attendance == null) state.attendance = new LinkedHashMap<>();
        if (state.materials == null) state.materials = new LinkedHashMap<>();
        if (state.notifications == null) state.notifications = new LinkedHashMap<>();

        for (User user : state.users.values()) {
            if (user instanceof Student student) {
                if (student.getEnrollments() == null) student.setEnrollments(new ArrayList<>());
            }
        }

        for (Course course : state.courses.values()) {
            course.updateAvailability();
        }
    }

    private static void addUserInternal(User user) {
        state.users.put(user.getUserId(), user);
    }

    private static void addCourseInternal(Course course) {
        state.courses.put(course.getCourseId(), course);
    }

    private static List<User> sortedUsers(List<User> users) {
        return users.stream().sorted(Comparator.comparing(User::getName, String.CASE_INSENSITIVE_ORDER)).collect(Collectors.toList());
    }

    public static List<User> getAllUsers() {
        synchronized (LOCK) {
            ensureInitialized();
            return sortedUsers(new ArrayList<>(state.users.values()));
        }
    }

    public static User findUserByUsername(String username) {
        synchronized (LOCK) {
            ensureInitialized();
            if (username == null) return null;
            return state.users.values().stream()
                    .filter(u -> u.getUsername().equalsIgnoreCase(username.trim()))
                    .findFirst()
                    .orElse(null);
        }
    }

    public static User findUserById(String userId) {
        synchronized (LOCK) {
            ensureInitialized();
            return state.users.get(userId);
        }
    }

    public static List<Student> getAllStudents() {
        synchronized (LOCK) {
            ensureInitialized();
            return state.users.values().stream()
                    .filter(Student.class::isInstance)
                    .map(Student.class::cast)
                    .sorted(Comparator.comparing(Student::getName, String.CASE_INSENSITIVE_ORDER))
                    .collect(Collectors.toList());
        }
    }

    public static List<Faculty> getAllFaculty() {
        synchronized (LOCK) {
            ensureInitialized();
            return state.users.values().stream()
                    .filter(Faculty.class::isInstance)
                    .map(Faculty.class::cast)
                    .sorted(Comparator.comparing(Faculty::getName, String.CASE_INSENSITIVE_ORDER))
                    .collect(Collectors.toList());
        }
    }

    public static boolean usernameExists(String username) {
        return findUserByUsername(username) != null;
    }

    public static boolean emailExists(String email) {
        synchronized (LOCK) {
            ensureInitialized();
            if (email == null) return false;
            return state.users.values().stream().anyMatch(u -> email.equalsIgnoreCase(u.getEmail()));
        }
    }

    public static OperationResult<User> addUser(User user) {
        synchronized (LOCK) {
            ensureInitialized();
            if (user == null) return OperationResult.fail("Invalid user.");
            if (usernameExists(user.getUsername())) return OperationResult.fail("Username already taken.");
            if (emailExists(user.getEmail())) return OperationResult.fail("Email already registered.");
            addUserInternal(user);
            saveState();
            return OperationResult.ok("User created successfully.", user);
        }
    }

    public static OperationResult<User> updateUser(User updatedUser) {
        synchronized (LOCK) {
            ensureInitialized();
            if (updatedUser == null || !state.users.containsKey(updatedUser.getUserId())) {
                return OperationResult.fail("User not found.");
            }
            for (User user : state.users.values()) {
                if (!user.getUserId().equals(updatedUser.getUserId()) && user.getUsername().equalsIgnoreCase(updatedUser.getUsername())) {
                    return OperationResult.fail("Username already taken.");
                }
                if (!user.getUserId().equals(updatedUser.getUserId()) && user.getEmail().equalsIgnoreCase(updatedUser.getEmail())) {
                    return OperationResult.fail("Email already registered.");
                }
            }
            state.users.put(updatedUser.getUserId(), updatedUser);
            saveState();
            return OperationResult.ok("User updated successfully.", updatedUser);
        }
    }

    public static OperationResult<User> toggleUserActive(String userId) {
        synchronized (LOCK) {
            ensureInitialized();
            User user = state.users.get(userId);
            if (user == null) return OperationResult.fail("User not found.");
            user.setActive(!user.isActive());
            saveState();
            return OperationResult.ok(user.isActive() ? "User activated." : "User deactivated.", user);
        }
    }

    public static List<Course> getAllCourses() {
        synchronized (LOCK) {
            ensureInitialized();
            return state.courses.values().stream()
                    .peek(Course::updateAvailability)
                    .sorted(Comparator.comparing(Course::getCourseCode, String.CASE_INSENSITIVE_ORDER))
                    .collect(Collectors.toList());
        }
    }

    public static List<Course> getDepartmentCourses(String department) {
        synchronized (LOCK) {
            ensureInitialized();
            return getAllCourses().stream()
                    .filter(c -> c.getDepartment().equalsIgnoreCase(department))
                    .collect(Collectors.toList());
        }
    }

    public static List<Course> getAssignedCourses(Faculty faculty) {
        synchronized (LOCK) {
            ensureInitialized();
            if (faculty == null) return new ArrayList<>();
            return getAllCourses().stream()
                    .filter(c -> c.getFaculty() != null && c.getFaculty().getUserId().equals(faculty.getUserId()))
                    .collect(Collectors.toList());
        }
    }

    public static Course findCourseById(String courseId) {
        synchronized (LOCK) {
            ensureInitialized();
            return state.courses.get(courseId);
        }
    }

    public static Course findCourseByCode(String courseCode) {
        synchronized (LOCK) {
            ensureInitialized();
            if (courseCode == null) return null;
            return state.courses.values().stream()
                    .filter(c -> c.getCourseCode().equalsIgnoreCase(courseCode.trim()))
                    .findFirst()
                    .orElse(null);
        }
    }

    public static List<Course> searchCourses(String keyword) {
        synchronized (LOCK) {
            ensureInitialized();
            if (keyword == null || keyword.isBlank()) {
                return getAllCourses();
            }
            String q = keyword.trim().toLowerCase(Locale.ROOT);
            return getAllCourses().stream()
                    .filter(c -> c.getCourseName().toLowerCase(Locale.ROOT).contains(q)
                            || c.getCourseCode().toLowerCase(Locale.ROOT).contains(q)
                            || c.getDepartment().toLowerCase(Locale.ROOT).contains(q)
                            || c.getDescription().toLowerCase(Locale.ROOT).contains(q))
                    .collect(Collectors.toList());
        }
    }

    public static OperationResult<Course> addCourse(Course course) {
        synchronized (LOCK) {
            ensureInitialized();
            if (course == null) return OperationResult.fail("Invalid course.");
            if (findCourseByCode(course.getCourseCode()) != null) return OperationResult.fail("Course code already exists.");
            addCourseInternal(course);
            saveState();
            return OperationResult.ok("Course created successfully.", course);
        }
    }

    public static OperationResult<Course> updateCourse(String courseId, String courseCode, String courseName,
                                                       String description, int credits, int maxSeats,
                                                       String department, Float feeAmount) {
        synchronized (LOCK) {
            ensureInitialized();
            Course course = state.courses.get(courseId);
            if (course == null) return OperationResult.fail("Course not found.");
            Course duplicate = findCourseByCode(courseCode);
            if (duplicate != null && !duplicate.getCourseId().equals(courseId)) {
                return OperationResult.fail("Course code already exists.");
            }
            int occupiedSeats = course.getEnrolledCount();
            if (maxSeats < occupiedSeats) {
                return OperationResult.fail("Max seats cannot be lower than current active enrollment count.");
            }
            course.setCourseCode(courseCode);
            course.setCourseName(courseName);
            course.setDescription(description == null ? "" : description);
            course.setCredits(credits);
            course.setMaxSeats(maxSeats);
            course.setDepartment(department);
            if (feeAmount != null) course.setFeeAmount(feeAmount);
            course.updateAvailability();
            saveState();
            return OperationResult.ok("Course updated successfully.", course);
        }
    }

    public static OperationResult<Course> updateCourseFaculty(String courseId, String facultyUserId) {
        synchronized (LOCK) {
            ensureInitialized();
            Course course = state.courses.get(courseId);
            User user = state.users.get(facultyUserId);
            if (course == null || !(user instanceof Faculty faculty)) {
                return OperationResult.fail("Course or faculty not found.");
            }
            if (course.getFaculty() != null) {
                course.getFaculty().removeAssignedCourse(course);
            }
            course.setFaculty(faculty);
            faculty.addAssignedCourse(course);
            saveState();
            return OperationResult.ok("Faculty assigned successfully.", course);
        }
    }

    public static List<Enrollment> getAllEnrollments() {
        synchronized (LOCK) {
            ensureInitialized();
            return state.enrollments.values().stream()
                    .sorted(Comparator.comparing(Enrollment::getEnrollmentDate).reversed())
                    .collect(Collectors.toList());
        }
    }

    public static Enrollment findEnrollmentById(String enrollmentId) {
        synchronized (LOCK) {
            ensureInitialized();
            return state.enrollments.get(enrollmentId);
        }
    }

    public static List<Enrollment> getPendingEnrollments() {
        return getAllEnrollments().stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.PENDING)
                .collect(Collectors.toList());
    }

    public static List<Enrollment> getPendingEnrollmentsByDepartment(String department) {
        return getPendingEnrollments().stream()
                .filter(e -> e.getCourse().getDepartment().equalsIgnoreCase(department))
                .collect(Collectors.toList());
    }

    public static List<Enrollment> getEnrollmentsByStudent(Student student) {
        synchronized (LOCK) {
            ensureInitialized();
            if (student == null) return new ArrayList<>();
            return state.enrollments.values().stream()
                    .filter(e -> e.getStudent().getUserId().equals(student.getUserId()))
                    .sorted(Comparator.comparing(Enrollment::getEnrollmentDate).reversed())
                    .collect(Collectors.toList());
        }
    }

    public static List<Enrollment> getEnrollmentsByCourse(Course course) {
        synchronized (LOCK) {
            ensureInitialized();
            if (course == null) return new ArrayList<>();
            return state.enrollments.values().stream()
                    .filter(e -> e.getCourse().getCourseId().equals(course.getCourseId()))
                    .sorted(Comparator.comparing(Enrollment::getEnrollmentDate).reversed())
                    .collect(Collectors.toList());
        }
    }

    public static List<Enrollment> getVisibleCourseEnrollmentsForFaculty(Course course) {
        return getEnrollmentsByCourse(course).stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE
                        || e.getStatus() == EnrollmentStatus.COMPLETED
                        || e.getStatus() == EnrollmentStatus.APPROVED)
                .collect(Collectors.toList());
    }

    public static boolean hasBlockingEnrollment(Student student, Course course) {
        return getEnrollmentsByStudent(student).stream().anyMatch(e ->
                e.getCourse().getCourseId().equals(course.getCourseId())
                        && e.getStatus() != EnrollmentStatus.DROPPED
                        && e.getStatus() != EnrollmentStatus.REJECTED);
    }

    private static boolean satisfiesPrerequisite(Student student, String prerequisiteCourseId) {
        return getEnrollmentsByStudent(student).stream().anyMatch(e ->
                e.getCourse().getCourseId().equals(prerequisiteCourseId)
                        && (e.getStatus() == EnrollmentStatus.ACTIVE
                        || e.getStatus() == EnrollmentStatus.COMPLETED));
    }

    public static List<String> getMissingPrerequisiteCodes(Student student, Course course) {
        List<String> missing = new ArrayList<>();
        String prereqString = course.getPrerequisites();
        if (prereqString != null && !prereqString.isEmpty()) {
            String[] prerequisiteCodes = prereqString.split(",");
            for (String prerequisiteCode : prerequisiteCodes) {
                String trimmedCode = prerequisiteCode.trim();
                if (!trimmedCode.isEmpty()) {
                    Course prerequisite = findCourseByCode(trimmedCode);
                    if (prerequisite != null && !satisfiesPrerequisite(student, prerequisite.getCourseId())) {
                        missing.add(trimmedCode);
                    }
                }
            }
        }
        return missing;
    }

    public static OperationResult<Enrollment> registerStudentForCourse(Student student, String courseId) {
        synchronized (LOCK) {
            ensureInitialized();
            Student storedStudent = (Student) findUserById(student.getUserId());
            Course course = findCourseById(courseId);
            if (storedStudent == null || course == null) return OperationResult.fail("Course not found.");
            course.updateAvailability();
            if (hasBlockingEnrollment(storedStudent, course)) {
                return OperationResult.fail("You have already registered for this course.");
            }
            if (!course.checkAvailability()) {
                return OperationResult.fail("Course is full.");
            }
            List<String> missing = getMissingPrerequisiteCodes(storedStudent, course);
            if (!missing.isEmpty()) {
                return OperationResult.fail("Missing prerequisite(s): " + String.join(", ", missing));
            }
            Enrollment enrollment = new Enrollment(id("ENR"), storedStudent, course, storedStudent.getSemester(), currentAcademicYear());
            state.enrollments.put(enrollment.getEnrollmentId(), enrollment);
            storedStudent.addEnrollment(enrollment);
            course.addEnrollment(enrollment);
            saveState();
            return OperationResult.ok("Registration submitted. Awaiting department head approval.", enrollment);
        }
    }

    public static OperationResult<Enrollment> approveEnrollment(String enrollmentId, String approverUserId) {
        synchronized (LOCK) {
            ensureInitialized();
            Enrollment enrollment = state.enrollments.get(enrollmentId);
            if (enrollment == null) return OperationResult.fail("Enrollment not found.");
            if (enrollment.getStatus() != EnrollmentStatus.PENDING) {
                return OperationResult.fail("Only pending enrollments can be approved.");
            }
            Course course = enrollment.getCourse();
            course.updateAvailability();
            if (!course.checkAvailability()) {
                return OperationResult.fail("Cannot approve. Course is already full.");
            }
            enrollment.approve(approverUserId);
            course.updateAvailability();
            ensurePaymentForEnrollment(enrollment);
            NotificationUtil.sendEnrollmentApprovalNotification(enrollment.getStudent(), course.getCourseName());
            saveState();
            return OperationResult.ok("Enrollment approved.", enrollment);
        }
    }

    public static OperationResult<Enrollment> rejectEnrollment(String enrollmentId, String reason) {
        synchronized (LOCK) {
            ensureInitialized();
            Enrollment enrollment = state.enrollments.get(enrollmentId);
            if (enrollment == null) return OperationResult.fail("Enrollment not found.");
            if (enrollment.getStatus() != EnrollmentStatus.PENDING) {
                return OperationResult.fail("Only pending enrollments can be rejected.");
            }
            enrollment.reject(reason == null || reason.isBlank() ? "Rejected by department head" : reason);
            enrollment.getCourse().updateAvailability();
            NotificationUtil.sendEnrollmentRejectionNotification(
                    enrollment.getStudent(), enrollment.getCourse().getCourseName(), enrollment.getRejectionReason());
            saveState();
            return OperationResult.ok("Enrollment rejected.", enrollment);
        }
    }

    public static OperationResult<Enrollment> dropEnrollment(String enrollmentId, String studentUserId) {
        synchronized (LOCK) {
            ensureInitialized();
            Enrollment enrollment = state.enrollments.get(enrollmentId);
            if (enrollment == null) return OperationResult.fail("Enrollment not found.");
            if (!enrollment.getStudent().getUserId().equals(studentUserId)) {
                return OperationResult.fail("You cannot drop another student's enrollment.");
            }
            if (enrollment.getStatus() == EnrollmentStatus.COMPLETED) {
                return OperationResult.fail("Completed courses cannot be dropped.");
            }
            if (enrollment.getStatus() == EnrollmentStatus.DROPPED) {
                return OperationResult.fail("This enrollment has already been dropped.");
            }
            EnrollmentStatus previousStatus = enrollment.getStatus();
            enrollment.drop();
            enrollment.getCourse().updateAvailability();
            Payment payment = findPaymentByEnrollmentId(enrollment.getEnrollmentId());
            if (payment != null && payment.getStatus() == PaymentStatus.COMPLETED && previousStatus != EnrollmentStatus.PENDING) {
                payment.refund();
            }
            saveState();
            return OperationResult.ok("Course dropped successfully.", enrollment);
        }
    }

    public static Payment ensurePaymentForEnrollment(Enrollment enrollment) {
        synchronized (LOCK) {
            ensureInitialized();
            Payment existing = findPaymentByEnrollmentId(enrollment.getEnrollmentId());
            if (existing != null) return existing;
            Payment payment = new Payment(id("PAY"), enrollment.getStudent(), enrollment.getCourse(), enrollment.getCourse().getFeeAmount());
            state.payments.put(payment.getPaymentId(), payment);
            enrollment.getStudent().addPayment(payment);
            saveState();
            return payment;
        }
    }

    public static Payment findPaymentById(String paymentId) {
        synchronized (LOCK) {
            ensureInitialized();
            return state.payments.get(paymentId);
        }
    }

    public static Payment findPaymentByEnrollmentId(String enrollmentId) {
        synchronized (LOCK) {
            ensureInitialized();
            Enrollment enrollment = findEnrollmentById(enrollmentId);
            if (enrollment == null) return null;
            return state.payments.values().stream()
                    .filter(p -> p.getStudent().getUserId().equals(enrollment.getStudent().getUserId())
                            && p.getCourse().getCourseId().equals(enrollment.getCourse().getCourseId()))
                    .findFirst()
                    .orElse(null);
        }
    }

    public static List<Payment> getAllPayments() {
        synchronized (LOCK) {
            ensureInitialized();
            return state.payments.values().stream()
                    .sorted(Comparator.comparing(Payment::getDueDate))
                    .collect(Collectors.toList());
        }
    }

    public static List<Payment> getPaymentsByStudent(Student student) {
        synchronized (LOCK) {
            ensureInitialized();
            if (student == null) return new ArrayList<>();
            return state.payments.values().stream()
                    .filter(p -> p.getStudent().getUserId().equals(student.getUserId()))
                    .sorted(Comparator.comparing(Payment::getDueDate))
                    .collect(Collectors.toList());
        }
    }

    public static List<Payment> getPendingPayments(Student student) {
        return getPaymentsByStudent(student).stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING || p.getStatus() == PaymentStatus.FAILED)
                .collect(Collectors.toList());
    }

    public static OperationResult<Payment> processPayment(String paymentId, String paymentMethod, boolean simulateFailure) {
        synchronized (LOCK) {
            ensureInitialized();
            Payment payment = state.payments.get(paymentId);
            if (payment == null) return OperationResult.fail("Payment not found.");
            if (payment.getStatus() == PaymentStatus.COMPLETED) return OperationResult.fail("Payment already completed.");
            boolean ok = payment.processPayment(paymentMethod, simulateFailure);
            saveState();
            if (ok) {
                NotificationUtil.sendPaymentSuccessNotification(
                        payment.getStudent(), payment.getCourse().getCourseName(), payment.getAmount());
                return OperationResult.ok("Payment completed successfully.", payment);
            }
            NotificationUtil.sendPaymentFailureNotification(
                    payment.getStudent(), payment.getCourse().getCourseName());
            return OperationResult.fail("Payment failed. You can retry");
        }
    }

    public static OperationResult<Grade> saveGrade(String enrollmentId, float marks, float totalMarks, String examType) {
        synchronized (LOCK) {
            ensureInitialized();
            Enrollment enrollment = state.enrollments.get(enrollmentId);
            if (enrollment == null) return OperationResult.fail("Enrollment not found.");
            if (marks < 0 || totalMarks <= 0 || marks > totalMarks) {
                return OperationResult.fail("Marks must be between 0 and total marks.");
            }
            if (enrollment.getStatus() != EnrollmentStatus.ACTIVE && enrollment.getStatus() != EnrollmentStatus.COMPLETED) {
                return OperationResult.fail("Grades can only be submitted for active enrollments.");
            }
            Grade grade = new Grade(id("GRD"), enrollment, marks, totalMarks, examType == null ? "Final" : examType);
            saveState();
            return OperationResult.ok("Grade submitted for review.", grade);
        }
    }

    public static Grade findGradeById(String gradeId) {
        synchronized (LOCK) {
            ensureInitialized();
            // Grades are now stored in the database via JPA, not in memory
            return null;
        }
    }

    public static List<Grade> getGradesByEnrollment(String enrollmentId) {
        synchronized (LOCK) {
            ensureInitialized();
            // Grades are now stored in the database via JPA
            // Use GradeRepository.findByEnrollmentId(enrollmentId) instead
            return new ArrayList<>();
        }
    }

    public static List<Grade> getGradesByCourse(String courseId) {
        synchronized (LOCK) {
            ensureInitialized();
            // Grades are now stored in the database via JPA
            // Use GradeRepository.findByCourseId(courseId) instead
            return new ArrayList<>();
        }
    }

    public static List<Grade> getPendingGradesByDepartment(String department) {
        return getDepartmentCourses(department).stream()
                .flatMap(c -> getGradesByCourse(c.getCourseId()).stream())
                .filter(g -> !g.isApproved() && g.getReviewedBy() == null)
                .collect(Collectors.toList());
    }

    public static OperationResult<Grade> approveGrade(String gradeId, String reviewerUserId, String comments) {
        synchronized (LOCK) {
            ensureInitialized();
            Grade grade = findGradeById(gradeId);
            if (grade == null) return OperationResult.fail("Grade not found.");
            grade.approve(reviewerUserId, comments == null || comments.isBlank() ? "Approved by department head" : comments);
            Enrollment enrollment = grade.getEnrollment();
            if (grade.getExamType() != null && grade.getExamType().trim().equalsIgnoreCase("final")) {
                enrollment.complete();
                enrollment.getCourse().updateAvailability();
            }
            enrollment.getStudent().calculateCGPA();
            NotificationUtil.sendGradePublishedNotification(
                    enrollment.getStudent(), enrollment.getCourse().getCourseName(), grade.getGrade());
            saveState();
            return OperationResult.ok("Grade approved.", grade);
        }
    }

    public static OperationResult<Grade> rejectGrade(String gradeId, String reviewerUserId, String comments) {
        synchronized (LOCK) {
            ensureInitialized();
            Grade grade = findGradeById(gradeId);
            if (grade == null) return OperationResult.fail("Grade not found.");
            grade.reject(reviewerUserId, comments == null || comments.isBlank() ? "Needs correction" : comments);
            saveState();
            return OperationResult.ok("Grade rejected and returned to faculty for correction.", grade);
        }
    }

    public static Attendance findAttendance(String courseId, String studentUserId, Date date) {
        synchronized (LOCK) {
            ensureInitialized();
            return state.attendance.values().stream()
                    .filter(a -> a.getCourse().getCourseId().equals(courseId)
                            && a.getStudent().getUserId().equals(studentUserId)
                            && isSameDay(a.getDate(), date))
                    .findFirst()
                    .orElse(null);
        }
    }

    public static OperationResult<Attendance> saveAttendance(String courseId, String studentUserId, Date date,
                                                             AttendanceStatus status, String markedBy, String remarks) {
        synchronized (LOCK) {
            ensureInitialized();
            Course course = state.courses.get(courseId);
            User studentUser = state.users.get(studentUserId);
            if (course == null || !(studentUser instanceof Student student)) {
                return OperationResult.fail("Course or student not found.");
            }
            boolean enrolled = getEnrollmentsByStudent(student).stream().anyMatch(e ->
                    e.getCourse().getCourseId().equals(courseId)
                            && (e.getStatus() == EnrollmentStatus.ACTIVE
                            || e.getStatus() == EnrollmentStatus.COMPLETED
                            || e.getStatus() == EnrollmentStatus.APPROVED));
            if (!enrolled) {
                return OperationResult.fail("Attendance can only be marked for enrolled students.");
            }
            Attendance existing = findAttendance(courseId, studentUserId, date);
            if (existing != null) {
                existing.setStatus(status);
                existing.setRemarks(remarks);
                existing.setDate(date);
                saveState();
                return OperationResult.ok("Existing attendance updated.", existing);
            }
            Attendance attendance = new Attendance(id("ATT"), student, course, date, status, markedBy, remarks);
            state.attendance.put(attendance.getAttendanceId(), attendance);
            student.addAttendanceRecord(attendance);
            float percentage = Attendance.getAttendancePercentage(student, course);
            if (percentage < 75.0f) {
                NotificationUtil.sendLowAttendanceWarning(student, course.getCourseName(), percentage);
            }
            saveState();
            return OperationResult.ok("Attendance recorded.", attendance);
        }
    }

    public static List<Attendance> getAttendanceByStudent(Student student) {
        synchronized (LOCK) {
            ensureInitialized();
            if (student == null) return new ArrayList<>();
            return state.attendance.values().stream()
                    .filter(a -> a.getStudent().getUserId().equals(student.getUserId()))
                    .sorted(Comparator.comparing(Attendance::getDate).reversed())
                    .collect(Collectors.toList());
        }
    }

    public static List<Attendance> getAttendanceByCourse(Course course) {
        synchronized (LOCK) {
            ensureInitialized();
            if (course == null) return new ArrayList<>();
            return state.attendance.values().stream()
                    .filter(a -> a.getCourse().getCourseId().equals(course.getCourseId()))
                    .sorted(Comparator.comparing(Attendance::getDate).reversed())
                    .collect(Collectors.toList());
        }
    }

    public static OperationResult<CourseMaterial> saveCourseMaterial(String courseId, String title, String description,
                                                                     String fileType, String filePath, long fileSize) {
        synchronized (LOCK) {
            ensureInitialized();
            Course course = state.courses.get(courseId);
            if (course == null) return OperationResult.fail("Course not found.");
            CourseMaterial material = new CourseMaterial(id("MAT"), title, description, fileType, filePath, course);
            material.setFileSize(fileSize);
            state.materials.put(material.getMaterialId(), material);
            course.addCourseMaterial(material);
            for (Enrollment enrollment : getEnrollmentsByCourse(course)) {
                if (enrollment.getStatus() == EnrollmentStatus.ACTIVE || enrollment.getStatus() == EnrollmentStatus.COMPLETED) {
                    NotificationUtil.sendMaterialUploadedNotification(enrollment.getStudent(), course.getCourseName(), title);
                }
            }
            saveState();
            return OperationResult.ok("Material uploaded successfully.", material);
        }
    }

    public static CourseMaterial findMaterialById(String materialId) {
        synchronized (LOCK) {
            ensureInitialized();
            return state.materials.get(materialId);
        }
    }

    public static List<CourseMaterial> getCourseMaterialsByCourse(Course course) {
        synchronized (LOCK) {
            ensureInitialized();
            if (course == null) return new ArrayList<>();
            return state.materials.values().stream()
                    .filter(m -> m.getCourse().getCourseId().equals(course.getCourseId()))
                    .sorted(Comparator.comparing(CourseMaterial::getUploadDate).reversed())
                    .collect(Collectors.toList());
        }
    }

    public static List<CourseMaterial> getCourseMaterialsForStudent(Student student) {
        synchronized (LOCK) {
            ensureInitialized();
            Set<String> accessibleCourseIds = getEnrollmentsByStudent(student).stream()
                    .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE
                            || e.getStatus() == EnrollmentStatus.COMPLETED
                            || e.getStatus() == EnrollmentStatus.APPROVED)
                    .map(e -> e.getCourse().getCourseId())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            return state.materials.values().stream()
                    .filter(m -> accessibleCourseIds.contains(m.getCourse().getCourseId()))
                    .sorted(Comparator.comparing(CourseMaterial::getUploadDate).reversed())
                    .collect(Collectors.toList());
        }
    }

    public static boolean studentCanAccessMaterial(Student student, String materialId) {
        synchronized (LOCK) {
            ensureInitialized();
            CourseMaterial material = state.materials.get(materialId);
            if (material == null) return false;
            return getCourseMaterialsForStudent(student).stream()
                    .anyMatch(m -> m.getMaterialId().equals(materialId));
        }
    }

    public static void addNotification(Notification notification) {
        synchronized (LOCK) {
            ensureInitialized();
            state.notifications.put(notification.getNotificationId(), notification);
            saveState();
        }
    }

    public static List<Notification> getNotificationsByUser(User user) {
        synchronized (LOCK) {
            ensureInitialized();
            if (user == null) return new ArrayList<>();
            return state.notifications.values().stream()
                    .filter(n -> n.getRecipient().getUserId().equals(user.getUserId()))
                    .sorted(Comparator.comparing(Notification::getSentDate).reversed())
                    .collect(Collectors.toList());
        }
    }

    public static String buildAdminReport() {
        ensureInitialized();
        StringBuilder sb = new StringBuilder();
        sb.append("SCM Admin Report\n");
        sb.append("Generated: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n\n");
        sb.append("Total Users: ").append(getAllUsers().size()).append("\n");
        sb.append("Students: ").append(getAllStudents().size()).append("\n");
        sb.append("Faculty: ").append(getAllFaculty().size()).append("\n");
        sb.append("Courses: ").append(getAllCourses().size()).append("\n");
        sb.append("Enrollments: ").append(getAllEnrollments().size()).append("\n");
        sb.append("Pending Enrollments: ").append(getPendingEnrollments().size()).append("\n");
        sb.append("Payments: ").append(getAllPayments().size()).append("\n");
        return sb.toString();
    }

    public static String buildDepartmentReport(String department) {
        ensureInitialized();
        List<Course> courses = getDepartmentCourses(department);
        StringBuilder sb = new StringBuilder();
        sb.append("Department Report - ").append(department).append("\n");
        sb.append("Generated: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n\n");
        sb.append("Courses: ").append(courses.size()).append("\n");
        sb.append("Pending Enrollments: ").append(getPendingEnrollmentsByDepartment(department).size()).append("\n");
        sb.append("Pending Grade Reviews: ").append(getPendingGradesByDepartment(department).size()).append("\n\n");
        for (Course course : courses) {
            sb.append(course.getCourseCode()).append(" - ").append(course.getCourseName())
                    .append(" | seats ").append(course.getAvailableSeats()).append("/").append(course.getMaxSeats())
                    .append(" | faculty ").append(course.getFaculty() != null ? course.getFaculty().getName() : "Unassigned")
                    .append("\n");
        }
        return sb.toString();
    }

    public static void updateEnrollmentStatus(String enrollmentId, String status) {
        synchronized (LOCK) {
            ensureInitialized();
            Enrollment enrollment = state.enrollments.get(enrollmentId);
            if (enrollment == null) return;
            try {
                enrollment.setStatus(EnrollmentStatus.valueOf(status));
                enrollment.getCourse().updateAvailability();
                saveState();
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public static void dropEnrollment(String enrollmentId) {
        dropEnrollment(enrollmentId, state.enrollments.get(enrollmentId) != null ? state.enrollments.get(enrollmentId).getStudent().getUserId() : null);
    }

    public static String currentAcademicYear() {
        Date now = new Date();
        @SuppressWarnings("deprecation") int year = now.getYear() + 1900;
        return year + "-" + (year + 1);
    }

    private static boolean isSameDay(Date left, Date right) {
        if (left == null || right == null) return false;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return sdf.format(left).equals(sdf.format(right));
    }

    public static void printStatistics() {
        ensureInitialized();
        System.out.println(buildAdminReport());
    }
}
