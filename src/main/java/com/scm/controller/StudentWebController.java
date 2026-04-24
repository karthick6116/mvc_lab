package com.scm.controller;

import com.scm.enums.EnrollmentStatus;
import com.scm.model.Attendance;
import com.scm.model.Course;
import com.scm.model.CourseMaterial;
import com.scm.model.Enrollment;
import com.scm.model.Payment;
import com.scm.model.Student;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.scm.util.DataStore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/student")
public class StudentWebController extends WebControllerSupport {

    private Student getStudent(HttpSession session) {
        return getAuthenticatedUser(session, Student.class);
    }

    private String requireStudent(Student student) {
        return student == null ? "redirect:/login" : null;
    }

    private void addCommonAttributes(Student student, Model model) {
        model.addAttribute("student", student);
        model.addAttribute("notifications", DataStore.getNotificationsByUser(student));
        model.addAttribute("cgpa", student.getCgpa());
    }

    @GetMapping({"", "/courses"})
    public String courses(@RequestParam(required = false) String q,
                          HttpSession session,
                          Model model) {
        Student student = getStudent(session);
        String redirect = requireStudent(student);
        if (redirect != null) return redirect;

        List<Course> courses = DataStore.searchCourses(q);
        Map<String, Enrollment> enrollmentMap = new LinkedHashMap<>();
        for (Enrollment enrollment : DataStore.getEnrollmentsByStudent(student)) {
            if (enrollment.getStatus() != EnrollmentStatus.DROPPED && enrollment.getStatus() != EnrollmentStatus.REJECTED) {
                enrollmentMap.put(enrollment.getCourse().getCourseId(), enrollment);
            }
        }

        applyFlash(session, model);
        addCommonAttributes(student, model);
        model.addAttribute("search", q == null ? "" : q);
        model.addAttribute("courses", courses);
        model.addAttribute("enrollmentMap", enrollmentMap);
        model.addAttribute("courseCount", courses.size());
        return "student/courses";
    }

    @GetMapping("/courses/{courseId}")
    public String courseDetails(@PathVariable String courseId,
                                HttpSession session,
                                Model model) {
        Student student = getStudent(session);
        String redirect = requireStudent(student);
        if (redirect != null) return redirect;

        Course course = DataStore.findCourseById(courseId);
        if (course == null) {
            flashError(session, "Course not found.");
            return "redirect:/student/courses";
        }
        Enrollment currentEnrollment = DataStore.getEnrollmentsByStudent(student).stream()
                .filter(e -> e.getCourse().getCourseId().equals(courseId))
                .filter(e -> e.getStatus() != EnrollmentStatus.DROPPED && e.getStatus() != EnrollmentStatus.REJECTED)
                .findFirst()
                .orElse(null);

        applyFlash(session, model);
        addCommonAttributes(student, model);
        List<String> missingPrerequisites = DataStore.getMissingPrerequisiteCodes(student, course);
        model.addAttribute("course", course);
        model.addAttribute("currentEnrollment", currentEnrollment);
        model.addAttribute("materials", DataStore.getCourseMaterialsByCourse(course));
        model.addAttribute("studentsCount", DataStore.getVisibleCourseEnrollmentsForFaculty(course).size());
        model.addAttribute("missingPrerequisites", missingPrerequisites);
        model.addAttribute("missingPrereqText", String.join(", ", missingPrerequisites));
        return "student/course-details";
    }

    @PostMapping("/enroll")
    public String enroll(@RequestParam String courseId,
                         HttpSession session) {
        Student student = getStudent(session);
        String redirect = requireStudent(student);
        if (redirect != null) return redirect;

        DataStore.OperationResult<Enrollment> result = DataStore.registerStudentForCourse(student, courseId);
        if (result.isSuccess()) {
            flashSuccess(session, result.getMessage());
        } else {
            flashError(session, result.getMessage());
        }
        return "redirect:/student/courses";
    }

    @GetMapping("/enrollments")
    public String enrollments(HttpSession session, Model model) {
        Student student = getStudent(session);
        String redirect = requireStudent(student);
        if (redirect != null) return redirect;

        List<Enrollment> enrollments = DataStore.getEnrollmentsByStudent(student);
        Map<String, Payment> paymentMap = new LinkedHashMap<>();
        for (Enrollment enrollment : enrollments) {
            Payment payment = DataStore.findPaymentByEnrollmentId(enrollment.getEnrollmentId());
            if (payment != null) {
                paymentMap.put(enrollment.getEnrollmentId(), payment);
            }
        }

        applyFlash(session, model);
        addCommonAttributes(student, model);
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("paymentMap", paymentMap);
        return "student/enrollments";
    }

    @PostMapping("/enrollments/drop")
    public String dropEnrollment(@RequestParam String enrollmentId,
                                 HttpSession session) {
        Student student = getStudent(session);
        String redirect = requireStudent(student);
        if (redirect != null) return redirect;

        DataStore.OperationResult<Enrollment> result = DataStore.dropEnrollment(enrollmentId, student.getUserId());
        if (result.isSuccess()) {
            flashSuccess(session, result.getMessage());
        } else {
            flashError(session, result.getMessage());
        }
        return "redirect:/student/enrollments";
    }

    @GetMapping("/grades")
    public String grades(HttpSession session, Model model) {
        Student student = getStudent(session);
        String redirect = requireStudent(student);
        if (redirect != null) return redirect;

        List<Enrollment> enrollments = DataStore.getEnrollmentsByStudent(student).stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.COMPLETED || e.getStatus() == EnrollmentStatus.ACTIVE)
                .collect(Collectors.toList());

        applyFlash(session, model);
        addCommonAttributes(student, model);
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("hasAnyGrades", !enrollments.isEmpty());
        return "student/grades";
    }

    @GetMapping("/schedule")
    public String schedule(HttpSession session, Model model) {
        Student student = getStudent(session);
        String redirect = requireStudent(student);
        if (redirect != null) return redirect;

        List<Enrollment> enrollments = DataStore.getEnrollmentsByStudent(student).stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE
                        || e.getStatus() == EnrollmentStatus.COMPLETED
                        || e.getStatus() == EnrollmentStatus.APPROVED)
                .collect(Collectors.toList());

        applyFlash(session, model);
        addCommonAttributes(student, model);
        model.addAttribute("enrollments", enrollments);
        return "student/schedule";
    }

    @GetMapping("/attendance")
    public String attendance(HttpSession session, Model model) {
        Student student = getStudent(session);
        String redirect = requireStudent(student);
        if (redirect != null) return redirect;

        List<Attendance> attendanceRecords = DataStore.getAttendanceByStudent(student);
        Map<String, String> percentageMap = new LinkedHashMap<>();
        for (Course course : student.getEnrolledCourses()) {
            float percentage = Attendance.getAttendancePercentage(student, course);
            percentageMap.put(course.getCourseId(), String.format(Locale.ROOT, "%.1f%%", percentage));
        }

        applyFlash(session, model);
        addCommonAttributes(student, model);
        model.addAttribute("attendanceRecords", attendanceRecords);
        model.addAttribute("percentageMap", percentageMap);
        model.addAttribute("dateFormat", new SimpleDateFormat("yyyy-MM-dd"));
        return "student/attendance";
    }

    @GetMapping("/materials")
    public String materials(HttpSession session, Model model) {
        Student student = getStudent(session);
        String redirect = requireStudent(student);
        if (redirect != null) return redirect;

        List<CourseMaterial> materials = DataStore.getCourseMaterialsForStudent(student);
        applyFlash(session, model);
        addCommonAttributes(student, model);
        model.addAttribute("materials", materials);
        return "student/materials";
    }

    @GetMapping("/materials/download/{materialId}")
    public void downloadMaterial(@PathVariable String materialId,
                                 HttpSession session,
                                 HttpServletResponse response) throws IOException {
        Student student = getStudent(session);
        if (student == null || !DataStore.studentCanAccessMaterial(student, materialId)) {
            response.sendRedirect("/login");
            return;
        }
        CourseMaterial material = DataStore.findMaterialById(materialId);
        if (material == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Path path = Paths.get(material.getFilePath());
        if (!Files.exists(path)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String contentType = Files.probeContentType(path);
        response.setContentType(contentType != null ? contentType : "application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + path.getFileName() + "\"");
        Files.copy(path, response.getOutputStream());
        response.getOutputStream().flush();
    }

    @GetMapping("/payments")
    public String payments(HttpSession session, Model model) {
        Student student = getStudent(session);
        String redirect = requireStudent(student);
        if (redirect != null) return redirect;

        List<Payment> payments = DataStore.getPaymentsByStudent(student);
        applyFlash(session, model);
        addCommonAttributes(student, model);
        model.addAttribute("payments", payments);
        model.addAttribute("pendingPayments", DataStore.getPendingPayments(student));
        return "student/payments";
    }

    @PostMapping("/payments/pay")
    public String processPayment(@RequestParam String paymentId,
                                 @RequestParam String paymentMethod,
                                 @RequestParam(required = false, defaultValue = "false") boolean simulateFailure,
                                 HttpSession session) {
        Student student = getStudent(session);
        String redirect = requireStudent(student);
        if (redirect != null) return redirect;

        Payment payment = DataStore.findPaymentById(paymentId);
        if (payment == null || !payment.getStudent().getUserId().equals(student.getUserId())) {
            flashError(session, "Payment not found.");
            return "redirect:/student/payments";
        }

        DataStore.OperationResult<Payment> result = DataStore.processPayment(paymentId, paymentMethod, simulateFailure);
        if (result.isSuccess()) {
            flashSuccess(session, result.getMessage());
        } else {
            flashError(session, result.getMessage());
        }
        return "redirect:/student/payments";
    }
}
