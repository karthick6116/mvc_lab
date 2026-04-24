package com.scm.controller;

import com.scm.model.Faculty;
import com.scm.model.Student;
import com.scm.model.User;
import com.scm.repository.UserRepository;
import com.scm.util.DataStore;
import com.scm.util.ValidationUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController extends WebControllerSupport {

    @Autowired
    private UserRepository userRepository;

    private int parseIntOrDefault(String value, int fallback) {
        try {
            return value == null || value.isBlank() ? fallback : Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session, Model model) {
        Object raw = session.getAttribute("user");
        if (raw instanceof User user && user.isActive()) {
            return "redirect:/dashboard";
        }
        applyFlash(session, model);
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          HttpSession session,
                          Model model) {
        // First try to find user in database
        var userOptional = userRepository.findByUsername(username);
        User user = null;
        
        if (userOptional.isPresent()) {
            user = userOptional.get();
        } else {
            // Fallback to DataStore for backward compatibility
            user = DataStore.findUserByUsername(username);
        }
        
        if (user != null && user.login(username, password)) {
            session.setAttribute("user", user);
            return "redirect:/dashboard";
        }
        model.addAttribute("error", "Invalid username or password.");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/signup")
    public String signupPage(HttpSession session, Model model) {
        applyFlash(session, model);
        return "signup";
    }

    @PostMapping("/signup")
    public String doSignup(@RequestParam String role,
                           @RequestParam String name,
                           @RequestParam String username,
                           @RequestParam String email,
                           @RequestParam(required = false) String phone,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           @RequestParam(required = false) String semester,
                           @RequestParam(required = false) String major,
                           @RequestParam(required = false) String dob,
                           @RequestParam(required = false) String address,
                           @RequestParam(required = false) String admYear,
                           @RequestParam(required = false) String department,
                           @RequestParam(required = false) String designation,
                           @RequestParam(required = false) String qualification,
                           @RequestParam(required = false) String specialization,
                           @RequestParam(required = false) String joinDate,
                           @RequestParam(required = false) String officeRoom,
                           Model model) {
        
        // Preserve form data
        model.addAttribute("name", name != null ? name : "");
        model.addAttribute("username", username != null ? username : "");
        model.addAttribute("email", email != null ? email : "");
        model.addAttribute("phone", phone != null ? phone : "");
        model.addAttribute("semester", semester != null ? semester : "");
        model.addAttribute("major", major != null ? major : "");
        model.addAttribute("dob", dob != null ? dob : "");
        model.addAttribute("address", address != null ? address : "");
        model.addAttribute("admYear", admYear != null ? admYear : "");
        model.addAttribute("department", department != null ? department : "");
        model.addAttribute("designation", designation != null ? designation : "");
        model.addAttribute("qualification", qualification != null ? qualification : "");
        model.addAttribute("specialization", specialization != null ? specialization : "");
        model.addAttribute("joinDate", joinDate != null ? joinDate : "");
        model.addAttribute("officeRoom", officeRoom != null ? officeRoom : "");
        
        String normalizedRole = role == null ? "" : role.trim().toUpperCase();
        String cleanName = name == null ? "" : name.trim();
        String cleanUsername = username == null ? "" : username.trim();
        String cleanEmail = email == null ? "" : email.trim();
        String cleanPhone = phone == null ? "" : phone.trim();

        if (!ValidationUtil.isNotEmpty(cleanName) || !ValidationUtil.isNotEmpty(cleanUsername) || !ValidationUtil.isNotEmpty(cleanEmail)) {
            model.addAttribute("error", "Name, username, and email are required.");
            return "signup";
        }
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            return "signup";
        }
        if (!ValidationUtil.isValidPassword(password)) {
            model.addAttribute("error", "Password must be at least 6 characters long.");
            return "signup";
        }
        if (!ValidationUtil.isValidEmail(cleanEmail)) {
            model.addAttribute("error", "Please enter a valid email address.");
            return "signup";
        }
        if (ValidationUtil.isNotEmpty(cleanPhone) && !ValidationUtil.isValidPhone(cleanPhone)) {
            model.addAttribute("error", "Phone number must be in XXX-XXX-XXXX format.");
            return "signup";
        }
        
        // Check in database first, then DataStore
        if (userRepository.findByUsername(cleanUsername).isPresent() || DataStore.usernameExists(cleanUsername)) {
            model.addAttribute("error", "Username already taken.");
            return "signup";
        }
        if (userRepository.findByEmail(cleanEmail).isPresent() || DataStore.emailExists(cleanEmail)) {
            model.addAttribute("error", "Email already registered.");
            return "signup";
        }

        User user;
        switch (normalizedRole) {
            case "STUDENT": {
                int parsedSemester = parseIntOrDefault(semester, 1);
                int parsedAdmissionYear = parseIntOrDefault(admYear, 2024);
                user = new Student(
                        "U" + System.currentTimeMillis(),
                        cleanUsername,
                        cleanEmail,
                        password,
                        cleanName,
                        cleanPhone,
                        "S" + System.currentTimeMillis(),
                        parsedSemester,
                        parsedAdmissionYear,
                        major == null ? "" : major.trim(),
                        dob == null ? "" : dob.trim(),
                        address == null ? "" : address.trim()
                );
                break;
            }
            case "FACULTY": {
                user = new Faculty(
                        "U" + System.currentTimeMillis(),
                        cleanUsername,
                        cleanEmail,
                        password,
                        cleanName,
                        cleanPhone,
                        "F" + System.currentTimeMillis(),
                        department == null ? "" : department.trim(),
                        designation == null ? "" : designation.trim(),
                        qualification == null ? "" : qualification.trim(),
                        specialization == null ? "" : specialization.trim(),
                        joinDate == null ? "" : joinDate.trim(),
                        officeRoom == null ? "" : officeRoom.trim()
                );
                break;
            }
            default:
                model.addAttribute("error", "Unsupported role selected.");
                return "signup";
        }

        User savedUser;
        try {
            // Save to database
            savedUser = userRepository.save(user);
            // Also save to DataStore for backward compatibility
            DataStore.addUser(user);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create account: " + e.getMessage());
            return "signup";
        }
        
        model.addAttribute("success", "Account created successfully. You can now login.");
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session) {
        Object raw = session.getAttribute("user");
        if (!(raw instanceof User user) || !user.isActive()) {
            session.invalidate();
            return "redirect:/login";
        }
        return switch (user.getRole()) {
            case STUDENT -> "redirect:/student/courses";
            case FACULTY -> "redirect:/faculty/courses";
            case ADMINISTRATOR -> "redirect:/admin/dashboard";
            case DEPARTMENT_HEAD -> "redirect:/depthead/dashboard";
        };
    }
}
