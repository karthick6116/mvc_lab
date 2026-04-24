package com.scm.controller;

import com.scm.model.Course;
import com.scm.model.DepartmentHead;
import com.scm.model.Enrollment;
import com.scm.model.Grade;
import com.scm.util.DataStore;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/depthead")
public class DeptHeadWebController extends WebControllerSupport {

    private DepartmentHead getDeptHead(HttpSession session) {
        return getAuthenticatedUser(session, DepartmentHead.class);
    }

    private String requireDeptHead(DepartmentHead deptHead) {
        return deptHead == null ? "redirect:/login" : null;
    }

    private void addCommon(DepartmentHead deptHead, Model model) {
        model.addAttribute("depthead", deptHead);
        model.addAttribute("notifications", DataStore.getNotificationsByUser(deptHead));
    }

    @GetMapping({"", "/dashboard"})
    public String dashboard(HttpSession session, Model model) {
        DepartmentHead deptHead = getDeptHead(session);
        String redirect = requireDeptHead(deptHead);
        if (redirect != null) return redirect;

        List<Course> deptCourses = DataStore.getDepartmentCourses(deptHead.getDepartment());
        applyFlash(session, model);
        addCommon(deptHead, model);
        model.addAttribute("deptCourses", deptCourses);
        model.addAttribute("pendingEnrollments", DataStore.getPendingEnrollmentsByDepartment(deptHead.getDepartment()));
        model.addAttribute("pendingGrades", DataStore.getPendingGradesByDepartment(deptHead.getDepartment()));
        model.addAttribute("reportPreview", DataStore.buildDepartmentReport(deptHead.getDepartment()));
        return "depthead/dashboard";
    }

    @GetMapping("/enrollments")
    public String enrollments(HttpSession session, Model model) {
        DepartmentHead deptHead = getDeptHead(session);
        String redirect = requireDeptHead(deptHead);
        if (redirect != null) return redirect;

        List<Enrollment> allDeptEnrollments = DataStore.getAllEnrollments().stream()
                .filter(e -> e.getCourse().getDepartment().equalsIgnoreCase(deptHead.getDepartment()))
                .toList();

        applyFlash(session, model);
        addCommon(deptHead, model);
        model.addAttribute("pendingEnrollments", DataStore.getPendingEnrollmentsByDepartment(deptHead.getDepartment()));
        model.addAttribute("allEnrollments", allDeptEnrollments);
        return "depthead/enrollments";
    }

    @PostMapping("/enrollments/approve")
    public String approve(@RequestParam String enrollmentId, HttpSession session) {
        DepartmentHead deptHead = getDeptHead(session);
        String redirect = requireDeptHead(deptHead);
        if (redirect != null) return redirect;

        DataStore.OperationResult<Enrollment> result = DataStore.approveEnrollment(enrollmentId, deptHead.getUserId());
        if (result.isSuccess()) {
            flashSuccess(session, result.getMessage());
        } else {
            flashError(session, result.getMessage());
        }
        return "redirect:/depthead/enrollments";
    }

    @PostMapping("/enrollments/reject")
    public String reject(@RequestParam String enrollmentId,
                         @RequestParam(required = false) String reason,
                         HttpSession session) {
        DepartmentHead deptHead = getDeptHead(session);
        String redirect = requireDeptHead(deptHead);
        if (redirect != null) return redirect;

        DataStore.OperationResult<Enrollment> result = DataStore.rejectEnrollment(enrollmentId, reason);
        if (result.isSuccess()) {
            flashSuccess(session, result.getMessage());
        } else {
            flashError(session, result.getMessage());
        }
        return "redirect:/depthead/enrollments";
    }

    @GetMapping("/grades")
    public String grades(@RequestParam(required = false) String courseId,
                         HttpSession session,
                         Model model) {
        DepartmentHead deptHead = getDeptHead(session);
        String redirect = requireDeptHead(deptHead);
        if (redirect != null) return redirect;

        List<Course> deptCourses = DataStore.getDepartmentCourses(deptHead.getDepartment());
        Course selectedCourse = courseId == null || courseId.isBlank()
                ? (deptCourses.isEmpty() ? null : deptCourses.get(0))
                : DataStore.findCourseById(courseId);
        if (selectedCourse != null && !selectedCourse.getDepartment().equalsIgnoreCase(deptHead.getDepartment())) {
            selectedCourse = null;
        }

        applyFlash(session, model);
        addCommon(deptHead, model);
        model.addAttribute("courses", deptCourses);
        model.addAttribute("course", selectedCourse);
        model.addAttribute("grades", selectedCourse == null ? List.of() : DataStore.getGradesByCourse(selectedCourse.getCourseId()));
        return "depthead/grades";
    }

    @PostMapping("/grades/approve")
    public String approveGrade(@RequestParam String gradeId,
                               @RequestParam(required = false) String comments,
                               HttpSession session) {
        DepartmentHead deptHead = getDeptHead(session);
        String redirect = requireDeptHead(deptHead);
        if (redirect != null) return redirect;

        Grade grade = DataStore.findGradeById(gradeId);
        if (grade == null) {
            flashError(session, "Grade not found.");
            return "redirect:/depthead/grades";
        }
        DataStore.OperationResult<Grade> result = DataStore.approveGrade(gradeId, deptHead.getUserId(), comments);
        if (result.isSuccess()) {
            flashSuccess(session, result.getMessage());
        } else {
            flashError(session, result.getMessage());
        }
        return "redirect:/depthead/grades?courseId=" + grade.getEnrollment().getCourse().getCourseId();
    }

    @PostMapping("/grades/reject")
    public String rejectGrade(@RequestParam String gradeId,
                              @RequestParam(required = false) String comments,
                              HttpSession session) {
        DepartmentHead deptHead = getDeptHead(session);
        String redirect = requireDeptHead(deptHead);
        if (redirect != null) return redirect;

        Grade grade = DataStore.findGradeById(gradeId);
        if (grade == null) {
            flashError(session, "Grade not found.");
            return "redirect:/depthead/grades";
        }
        DataStore.OperationResult<Grade> result = DataStore.rejectGrade(gradeId, deptHead.getUserId(), comments);
        if (result.isSuccess()) {
            flashSuccess(session, result.getMessage());
        } else {
            flashError(session, result.getMessage());
        }
        return "redirect:/depthead/grades?courseId=" + grade.getEnrollment().getCourse().getCourseId();
    }

    @GetMapping("/report")
    public void report(HttpSession session, HttpServletResponse response) throws IOException {
        DepartmentHead deptHead = getDeptHead(session);
        if (deptHead == null) {
            response.sendRedirect("/login");
            return;
        }
        String content = DataStore.buildDepartmentReport(deptHead.getDepartment());
        response.setContentType("text/plain;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=department-report.txt");
        response.getOutputStream().write(content.getBytes(StandardCharsets.UTF_8));
        response.getOutputStream().flush();
    }
}
