package com.scm.controller;

import com.scm.model.Administrator;
import com.scm.model.Course;
import com.scm.model.User;
import com.scm.util.DataStore;
import com.scm.util.ValidationUtil;
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

@Controller
@RequestMapping("/admin")
public class AdminWebController extends WebControllerSupport {

    private Administrator getAdmin(HttpSession session) {
        return getAuthenticatedUser(session, Administrator.class);
    }

    private String requireAdmin(Administrator admin) {
        return admin == null ? "redirect:/login" : null;
    }

    private void addCommon(Administrator admin, Model model) {
        model.addAttribute("admin", admin);
        model.addAttribute("notifications", DataStore.getNotificationsByUser(admin));
    }

    @GetMapping({"", "/dashboard"})
    public String dashboard(HttpSession session, Model model) {
        Administrator admin = getAdmin(session);
        String redirect = requireAdmin(admin);
        if (redirect != null) return redirect;

        applyFlash(session, model);
        addCommon(admin, model);
        model.addAttribute("totalUsers", DataStore.getAllUsers().size());
        model.addAttribute("totalStudents", DataStore.getAllStudents().size());
        model.addAttribute("totalFaculty", DataStore.getAllFaculty().size());
        model.addAttribute("totalCourses", DataStore.getAllCourses().size());
        model.addAttribute("totalEnrollments", DataStore.getAllEnrollments().size());
        model.addAttribute("pendingEnrollments", DataStore.getPendingEnrollments().size());
        model.addAttribute("totalPayments", DataStore.getAllPayments().size());
        model.addAttribute("reportPreview", DataStore.buildAdminReport());
        return "admin/dashboard";
    }

    @GetMapping("/courses")
    public String courses(HttpSession session, Model model) {
        Administrator admin = getAdmin(session);
        String redirect = requireAdmin(admin);
        if (redirect != null) return redirect;

        applyFlash(session, model);
        addCommon(admin, model);
        model.addAttribute("courses", DataStore.getAllCourses());
        model.addAttribute("faculty", DataStore.getAllFaculty());
        return "admin/courses";
    }

    @PostMapping("/courses/create")
    public String createCourse(@RequestParam String courseCode,
                               @RequestParam String courseName,
                               @RequestParam int credits,
                               @RequestParam int maxSeats,
                               @RequestParam String department,
                               @RequestParam(required = false) String description,
                               @RequestParam(required = false) Float feeAmount,
                               HttpSession session) {
        Administrator admin = getAdmin(session);
        String redirect = requireAdmin(admin);
        if (redirect != null) return redirect;

        if (!ValidationUtil.isNotEmpty(courseCode) || !ValidationUtil.isNotEmpty(courseName) || !ValidationUtil.isNotEmpty(department)) {
            flashError(session, "Course code, name, and department are required.");
            return "redirect:/admin/courses";
        }
        if (!ValidationUtil.isValidCredits(credits)) {
            flashError(session, "Credits must be between 1 and 6.");
            return "redirect:/admin/courses";
        }
        if (!ValidationUtil.isValidSeats(maxSeats)) {
            flashError(session, "Seats must be between 1 and 100.");
            return "redirect:/admin/courses";
        }
        if (feeAmount != null && !ValidationUtil.isValidAmount(feeAmount)) {
            flashError(session, "Fee amount must be positive.");
            return "redirect:/admin/courses";
        }

        Course course = admin.createCourse(
                "C" + System.currentTimeMillis(),
                courseCode.trim().toUpperCase(),
                courseName.trim(),
                description == null ? "" : description.trim(),
                credits,
                maxSeats,
                department.trim()
        );
        if (feeAmount != null) {
            course.setFeeAmount(feeAmount);
        }
        DataStore.OperationResult<Course> result = DataStore.addCourse(course);
        if (result.isSuccess()) {
            flashSuccess(session, result.getMessage());
        } else {
            flashError(session, result.getMessage());
        }
        return "redirect:/admin/courses";
    }

    @PostMapping("/courses/update")
    public String updateCourse(@RequestParam String courseId,
                               @RequestParam String courseCode,
                               @RequestParam String courseName,
                               @RequestParam int credits,
                               @RequestParam int maxSeats,
                               @RequestParam String department,
                               @RequestParam(required = false) String description,
                               @RequestParam(required = false) Float feeAmount,
                               HttpSession session) {
        Administrator admin = getAdmin(session);
        String redirect = requireAdmin(admin);
        if (redirect != null) return redirect;

        if (!ValidationUtil.isNotEmpty(courseCode) || !ValidationUtil.isNotEmpty(courseName) || !ValidationUtil.isNotEmpty(department)) {
            flashError(session, "Course code, name, and department are required.");
            return "redirect:/admin/courses";
        }
        if (!ValidationUtil.isValidCredits(credits)) {
            flashError(session, "Credits must be between 1 and 6.");
            return "redirect:/admin/courses";
        }
        if (!ValidationUtil.isValidSeats(maxSeats)) {
            flashError(session, "Seats must be between 1 and 100.");
            return "redirect:/admin/courses";
        }

        DataStore.OperationResult<Course> result = DataStore.updateCourse(
                courseId,
                courseCode.trim().toUpperCase(),
                courseName.trim(),
                description == null ? "" : description.trim(),
                credits,
                maxSeats,
                department.trim(),
                feeAmount
        );
        if (result.isSuccess()) {
            flashSuccess(session, result.getMessage());
        } else {
            flashError(session, result.getMessage());
        }
        return "redirect:/admin/courses";
    }

    @PostMapping("/courses/assign-faculty")
    public String assignFaculty(@RequestParam String courseId,
                                @RequestParam String facultyUserId,
                                HttpSession session) {
        Administrator admin = getAdmin(session);
        String redirect = requireAdmin(admin);
        if (redirect != null) return redirect;

        DataStore.OperationResult<Course> result = DataStore.updateCourseFaculty(courseId, facultyUserId);
        if (result.isSuccess()) {
            flashSuccess(session, result.getMessage());
        } else {
            flashError(session, result.getMessage());
        }
        return "redirect:/admin/courses";
    }

    @GetMapping("/users")
    public String users(HttpSession session, Model model) {
        Administrator admin = getAdmin(session);
        String redirect = requireAdmin(admin);
        if (redirect != null) return redirect;

        applyFlash(session, model);
        addCommon(admin, model);
        model.addAttribute("users", DataStore.getAllUsers());
        return "admin/users";
    }

    @PostMapping("/users/toggle")
    public String toggleUser(@RequestParam String userId, HttpSession session) {
        Administrator admin = getAdmin(session);
        String redirect = requireAdmin(admin);
        if (redirect != null) return redirect;

        User target = DataStore.findUserById(userId);
        if (target == null) {
            flashError(session, "User not found.");
            return "redirect:/admin/users";
        }
        if (target.getUserId().equals(admin.getUserId())) {
            flashError(session, "You cannot deactivate your own admin account.");
            return "redirect:/admin/users";
        }
        DataStore.OperationResult<User> result = DataStore.toggleUserActive(userId);
        if (result.isSuccess()) {
            flashSuccess(session, result.getMessage());
        } else {
            flashError(session, result.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/enrollments")
    public String enrollments(HttpSession session, Model model) {
        Administrator admin = getAdmin(session);
        String redirect = requireAdmin(admin);
        if (redirect != null) return redirect;

        applyFlash(session, model);
        addCommon(admin, model);
        model.addAttribute("enrollments", DataStore.getAllEnrollments());
        model.addAttribute("payments", DataStore.getAllPayments());
        return "admin/enrollments";
    }

    @GetMapping("/report")
    public void report(HttpSession session, HttpServletResponse response) throws IOException {
        Administrator admin = getAdmin(session);
        if (admin == null) {
            response.sendRedirect("/login");
            return;
        }
        String content = DataStore.buildAdminReport();
        response.setContentType("text/plain;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=admin-report.txt");
        response.getOutputStream().write(content.getBytes(StandardCharsets.UTF_8));
        response.getOutputStream().flush();
    }
}
