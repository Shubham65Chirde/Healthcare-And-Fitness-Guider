package com.fitness.capstonefitness;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user) {
        userRepository.save(user);
        return "redirect:/login.html";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String email, @RequestParam String password, HttpSession session) {
        User user = userRepository.findByEmail(email);

        if (user != null && user.getPassword().equals(password)) {
            session.setAttribute("userName", user.getFullName());
            session.setAttribute("userEmail", user.getEmail());
            return "redirect:/dashboard.html";
        } else {
            return "redirect:/login.html?error=true";
        }
    }

    @PostMapping("/api/admin/login")
    @ResponseBody
    public String adminLogin(@RequestBody Map<String, String> payload, HttpSession session) {
        String username = payload.get("username");
        String password = payload.get("password");

        String sql = "SELECT count(*) FROM admins WHERE username = ? AND password = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username, password);

        if (count != null && count > 0) {
            session.setAttribute("adminUser", username);
            return "{\"status\":\"success\"}";
        } else {
            return "{\"status\":\"error\", \"message\":\"Invalid Admin Username or Password\"}";
        }
    }

    @GetMapping("/api/user-name")
    @ResponseBody
    public String getUserName(HttpSession session) {
        String name = (String) session.getAttribute("userName");
        return (name != null) ? name : "Guest";
    }

    @PostMapping("/api/save-workout")
    @ResponseBody
    public String saveWorkout(@RequestBody Map<String, Object> payload, HttpSession session) {
        String email = (String) session.getAttribute("userEmail");

        if (email == null) {
            return "{\"status\":\"error\", \"message\":\"Session expired\"}";
        }

        String type = (String) payload.get("type");
        int duration = Integer.parseInt(payload.get("duration").toString());
        int kcal = Integer.parseInt(payload.get("kcal").toString());
        String day = (String) payload.getOrDefault("day", java.time.LocalDate.now().getDayOfWeek().name());

        String sql = "INSERT INTO workout_logs (user_email, workout_type, workout_day, duration_min, kcal_burned) VALUES (?, ?, ?, ?, ?)";

        try {
            jdbcTemplate.update(sql, email, type, day, duration, kcal);
            return "{\"status\":\"success\"}";
        } catch (Exception e) {
            return "{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}";
        }
    }

    @PostMapping("/api/save-diet")
    @ResponseBody
    public String saveDiet(@RequestBody Map<String, Object> payload, HttpSession session) {
        String email = (String) session.getAttribute("userEmail");

        if (email == null) {
            return "{\"status\":\"error\", \"message\":\"Authentication required\"}";
        }

        String planType = (String) payload.get("plan_type");
        String dayName = (String) payload.get("day");
        int calories = Integer.parseInt(payload.get("calories").toString());
        String status = (String) payload.get("status");

        String sql = "INSERT INTO diet_logs (user_email, plan_type, day_name, calories, status) VALUES (?, ?, ?, ?, ?)";

        try {
            jdbcTemplate.update(sql, email, planType, dayName, calories, status);
            return "{\"status\":\"success\"}";
        } catch (Exception e) {
            return "{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}";
        }
    }

    @GetMapping("/api/my-progress")
    @ResponseBody
    public List<Map<String, Object>> getProgress(HttpSession session) {
        String email = (String) session.getAttribute("userEmail");

        if (email == null) {
            return new ArrayList<>();
        }

        String sql = "SELECT 'workout' as category, workout_type as type, workout_day as day, kcal_burned as kcal, duration_min, log_date FROM workout_logs WHERE user_email = ? " +
                "UNION ALL " +
                "SELECT 'diet' as category, plan_type as type, day_name as day, calories as kcal, 0 as duration_min, logged_at as log_date FROM diet_logs WHERE user_email = ? " +
                "ORDER BY log_date DESC";

        try {
            return jdbcTemplate.queryForList(sql, email, email);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @GetMapping("/api/admin/all-progress")
    @ResponseBody
    public List<Map<String, Object>> getAllProgress() {
        String sql = "SELECT u.full_name as name, u.email, " +
                "COALESCE(d.plan_type, 'No Plan') as diet, " +
                "COALESCE(w.workout_type, 'No Workout') as workout " +
                "FROM users u " +
                "LEFT JOIN diet_logs d ON u.email = d.user_email " +
                "LEFT JOIN workout_logs w ON u.email = w.user_email";
        return jdbcTemplate.queryForList(sql);
    }

    @GetMapping("/api/admin/user-full-data")
    @ResponseBody
    public Map<String, Object> getUserFullData(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();

        String workoutSql = "SELECT workout_type, workout_day, duration_min, kcal_burned, log_date FROM workout_logs WHERE user_email = ? ORDER BY log_date DESC";
        String dietSql = "SELECT plan_type, day_name, calories, status, logged_at FROM diet_logs WHERE user_email = ? ORDER BY logged_at DESC";

        response.put("workouts", jdbcTemplate.queryForList(workoutSql, email));
        response.put("diets", jdbcTemplate.queryForList(dietSql, email));

        return response;
    }

    @DeleteMapping("/api/admin/delete-user")
    @ResponseBody
    public Map<String, String> deleteUser(@RequestParam String email) {
        Map<String, String> response = new HashMap<>();
        try {
            jdbcTemplate.update("DELETE FROM workout_logs WHERE user_email = ?", email);
            jdbcTemplate.update("DELETE FROM diet_logs WHERE user_email = ?", email);
            jdbcTemplate.update("DELETE FROM users WHERE email = ?", email);

            response.put("status", "success");
            response.put("message", "User and associated data deleted successfully");
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PostMapping("/api/admin/send-pdf")
    @ResponseBody
    public String sendPdfEmail(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        try {
            return "{\"status\":\"success\", \"message\":\"Report sent to " + email + "\"}";
        } catch (Exception e) {
            return "{\"status\":\"error\", \"message\":\"Failed to send email\"}";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/index.html";
    }
}