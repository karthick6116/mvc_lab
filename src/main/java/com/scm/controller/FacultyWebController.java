package com.scm.controller;

import com.scm.enums.AttendanceStatus;
import com.scm.model.Attendance;
import com.scm.model.Course;
import com.scm.model.CourseMaterial;
import com.scm.model.Enrollment;
import com.scm.model.Faculty;
import com.scm.util.DataStore;
import com.scm.util.ValidationUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/faculty")
public class FacultyWebController extends WebControllerSupport {

    private static final long MAX_UPLOAD_BYTES = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "ppt", "pptx", "mp4", "avi", "mov", "txt");

    private Faculty getFaculty(HttpSession session) {
        return getAuthenticatedUser(session, Faculty.class);
    }

    private String requireFaculty(Faculty faculty) {
        return faculty == null ? "redirect:/login" : null;
    }

    private void addCommon(Faculty faculty, Model model) {
        model.addAttribute("faculty", faculty);
        model.addAttribute("notifications", DataStore.getNotificationsByUser(faculty));
    }

    private boolean ownsCourse(Faculty faculty, Course course) {
        return faculty != null && course != null && course.getFaculty() != null
                && course.getFaculty().getUserId().equals(faculty.getUserId());
    }

    private Date parseDate(String date) {
        if (date == null || date.isBlank()) {
            return new Date();
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(date);
        } catch (ParseException e) {
            return new Date();
        }
    }

    @GetMapping({"", "/courses"})
    public String courses(HttpSession session, Model model) {
        Faculty faculty = getFaculty(session);
        String redirect = requireFaculty(faculty);
        if (redirect != null) return redirect;

        applyFlash(session, model);
        addCommon(faculty, model);
        model.addAttribute("courses", DataStore.getAssignedCourses(faculty));
        return "faculty/courses";
    }

    @GetMapping("/students")
    public String students(@RequestParam(required = false) String courseId,
                           HttpSession session,
                           Model model) {
        Faculty faculty = getFaculty(session);
        String redirect = requireFaculty(faculty);
        if (redirect != null) return redirect;

        List<Course> myCourses = DataStore.getAssignedCourses(faculty);
        Course selectedCourse = courseId == null || courseId.isBlank()
                ? (myCourses.isEmpty() ? null : myCourses.get(0))
                : DataStore.findCourseById(courseId);
        if (selectedCourse != null && !ownsCourse(faculty, selectedCourse)) {
            selectedCourse = null;
        }

        applyFlash(session, model);
        addCommon(faculty, model);
        model.addAttribute("courses", myCourses);
        model.addAttribute("course", selectedCourse);
        model.addAttribute("enrollments", selectedCourse == null ? List.of() : DataStore.getVisibleCourseEnrollmentsForFaculty(selectedCourse));
        return "faculty/students";
    }

    @GetMapping("/grades")
    public String grades(@RequestParam(required = false) String courseId,
                         HttpSession session,
                         Model model) {
        Faculty faculty = getFaculty(session);
        String redirect = requireFaculty(faculty);
        if (redirect != null) return redirect;

        List<Course> myCourses = DataStore.getAssignedCourses(faculty);
        Course selectedCourse = courseId == null || courseId.isBlank()
                ? (myCourses.isEmpty() ? null : myCourses.get(0))
                : DataStore.findCourseById(courseId);
        if (selectedCourse != null && !ownsCourse(faculty, selectedCourse)) {
            flashError(session, "You cannot manage another faculty member's course.");
            return "redirect:/faculty/grades";
        }

        applyFlash(session, model);
        addCommon(faculty, model);
        model.addAttribute("courses", myCourses);
        model.addAttribute("course", selectedCourse);
        model.addAttribute("enrollments", selectedCourse == null ? List.of() : DataStore.getVisibleCourseEnrollmentsForFaculty(selectedCourse));
        return "faculty/grades";
    }

    @PostMapping("/grades/submit")
    public String submitGrade(@RequestParam String enrollmentId,
                              @RequestParam float marks,
                              @RequestParam float totalMarks,
                              @RequestParam(required = false, defaultValue = "Final") String examType,
                              HttpSession session) {
        Faculty faculty = getFaculty(session);
        String redirect = requireFaculty(faculty);
        if (redirect != null) return redirect;

        Enrollment enrollment = DataStore.findEnrollmentById(enrollmentId);
        if (enrollment == null || !ownsCourse(faculty, enrollment.getCourse())) {
            flashError(session, "Enrollment not found.");
            return "redirect:/faculty/grades";
        }
        if (!ValidationUtil.isValidMarks(marks, totalMarks)) {
            flashError(session, "Marks must be between 0 and total marks.");
            return "redirect:/faculty/grades?courseId=" + enrollment.getCourse().getCourseId();
        }

        DataStore.OperationResult<?> result = DataStore.saveGrade(enrollmentId, marks, totalMarks, examType);
        if (result.isSuccess()) {
            flashSuccess(session, result.getMessage());
        } else {
            flashError(session, result.getMessage());
        }
        return "redirect:/faculty/grades?courseId=" + enrollment.getCourse().getCourseId();
    }

    @GetMapping("/attendance")
    public String attendance(@RequestParam(required = false) String courseId,
                             @RequestParam(required = false) String date,
                             HttpSession session,
                             Model model) {
        Faculty faculty = getFaculty(session);
        String redirect = requireFaculty(faculty);
        if (redirect != null) return redirect;

        List<Course> myCourses = DataStore.getAssignedCourses(faculty);
        Course selectedCourse = courseId == null || courseId.isBlank()
                ? (myCourses.isEmpty() ? null : myCourses.get(0))
                : DataStore.findCourseById(courseId);
        if (selectedCourse != null && !ownsCourse(faculty, selectedCourse)) {
            flashError(session, "You cannot manage another faculty member's course.");
            return "redirect:/faculty/attendance";
        }
        String selectedDate = (date == null || date.isBlank())
                ? new SimpleDateFormat("yyyy-MM-dd").format(new Date())
                : date;
        Date actualDate = parseDate(selectedDate);

        List<Enrollment> enrollments = selectedCourse == null ? List.of() : DataStore.getVisibleCourseEnrollmentsForFaculty(selectedCourse);
        Map<String, Attendance> attendanceMap = new LinkedHashMap<>();
        for (Enrollment enrollment : enrollments) {
            Attendance existing = DataStore.findAttendance(selectedCourse.getCourseId(), enrollment.getStudent().getUserId(), actualDate);
            if (existing != null) {
                attendanceMap.put(enrollment.getStudent().getUserId(), existing);
            }
        }

        applyFlash(session, model);
        addCommon(faculty, model);
        model.addAttribute("courses", myCourses);
        model.addAttribute("course", selectedCourse);
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("attendanceMap", attendanceMap);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("statuses", Arrays.asList(AttendanceStatus.values()));
        return "faculty/attendance";
    }

    @PostMapping("/attendance/mark")
    public String markAttendance(@RequestParam String courseId,
                                 @RequestParam String studentUserId,
                                 @RequestParam String date,
                                 @RequestParam AttendanceStatus status,
                                 @RequestParam(required = false) String remarks,
                                 HttpSession session) {
        Faculty faculty = getFaculty(session);
        String redirect = requireFaculty(faculty);
        if (redirect != null) return redirect;

        Course course = DataStore.findCourseById(courseId);
        if (!ownsCourse(faculty, course)) {
            flashError(session, "Course not found.");
            return "redirect:/faculty/attendance";
        }

        DataStore.OperationResult<Attendance> result = DataStore.saveAttendance(
                courseId,
                studentUserId,
                parseDate(date),
                status,
                faculty.getUserId(),
                remarks
        );
        if (result.isSuccess()) {
            flashSuccess(session, result.getMessage());
        } else {
            flashError(session, result.getMessage());
        }
        return "redirect:/faculty/attendance?courseId=" + courseId + "&date=" + date;
    }

    @GetMapping("/materials")
    public String materials(@RequestParam(required = false) String courseId,
                            HttpSession session,
                            Model model) {
        Faculty faculty = getFaculty(session);
        String redirect = requireFaculty(faculty);
        if (redirect != null) return redirect;

        List<Course> myCourses = DataStore.getAssignedCourses(faculty);
        Course selectedCourse = courseId == null || courseId.isBlank()
                ? (myCourses.isEmpty() ? null : myCourses.get(0))
                : DataStore.findCourseById(courseId);
        if (selectedCourse != null && !ownsCourse(faculty, selectedCourse)) {
            flashError(session, "You cannot manage another faculty member's course.");
            return "redirect:/faculty/materials";
        }

        applyFlash(session, model);
        addCommon(faculty, model);
        model.addAttribute("courses", myCourses);
        model.addAttribute("course", selectedCourse);
        model.addAttribute("materials", selectedCourse == null ? List.of() : DataStore.getCourseMaterialsByCourse(selectedCourse));
        return "faculty/materials";
    }

    @PostMapping("/materials/upload")
    public String uploadMaterial(@RequestParam String courseId,
                                 @RequestParam String title,
                                 @RequestParam(required = false) String description,
                                 @RequestParam("file") MultipartFile file,
                                 HttpSession session) {
        Faculty faculty = getFaculty(session);
        String redirect = requireFaculty(faculty);
        if (redirect != null) return redirect;

        Course course = DataStore.findCourseById(courseId);
        if (!ownsCourse(faculty, course)) {
            flashError(session, "Course not found.");
            return "redirect:/faculty/materials";
        }
        if (!ValidationUtil.isNotEmpty(title)) {
            flashError(session, "Material title is required.");
            return "redirect:/faculty/materials?courseId=" + courseId;
        }
        if (file == null || file.isEmpty()) {
            flashError(session, "Please choose a file to upload.");
            return "redirect:/faculty/materials?courseId=" + courseId;
        }
        if (file.getSize() > MAX_UPLOAD_BYTES) {
            flashError(session, "File too large. Maximum size is 10 MB.");
            return "redirect:/faculty/materials?courseId=" + courseId;
        }
        String originalName = file.getOriginalFilename() == null ? "material" : file.getOriginalFilename();
        String extension = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT) : "";
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            flashError(session, "Invalid file type. Allowed: pdf, ppt, pptx, mp4, avi, mov, txt.");
            return "redirect:/faculty/materials?courseId=" + courseId;
        }

        try {
            Path courseDir = DataStore.getMaterialsDir().resolve(courseId);
            Files.createDirectories(courseDir);
            String safeName = System.currentTimeMillis() + "_" + originalName.replaceAll("[^A-Za-z0-9._-]", "_");
            Path target = courseDir.resolve(safeName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            DataStore.OperationResult<CourseMaterial> result = DataStore.saveCourseMaterial(
                    courseId,
                    title.trim(),
                    description == null ? "" : description.trim(),
                    extension,
                    target.toAbsolutePath().toString(),
                    file.getSize()
            );
            if (result.isSuccess()) {
                flashSuccess(session, result.getMessage());
            } else {
                flashError(session, result.getMessage());
            }
        } catch (IOException ex) {
            flashError(session, "Unable to upload file: " + ex.getMessage());
        }
        return "redirect:/faculty/materials?courseId=" + courseId;
    }
}
