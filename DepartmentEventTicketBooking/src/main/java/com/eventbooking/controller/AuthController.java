package com.eventbooking.controller;

import com.eventbooking.model.User;
import com.eventbooking.service.EmailService;
import com.eventbooking.service.OtpService;
import com.eventbooking.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    // ─── Registration ────────────────────────────────────────────────────────

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        if (userService.usernameExists(user.getUsername())) {
            redirectAttributes.addFlashAttribute("error", "Username already exists!");
            return "redirect:/register";
        }
        userService.registerUser(user);
        redirectAttributes.addFlashAttribute("message", "Registration successful! Please login.");
        return "redirect:/login";
    }

    // ─── Login OTP ───────────────────────────────────────────────────────────

    @GetMapping("/verify-login-otp")
    public String showLoginOtpPage(HttpSession session, Model model) {
        String username = (String) session.getAttribute("pendingLoginUsername");
        if (username == null) return "redirect:/login";

        User user = userService.findByUsername(username);
        // Mask email for display: g***@gmail.com
        String maskedEmail = maskEmail(user != null ? user.getEmail() : "");
        model.addAttribute("maskedEmail", maskedEmail);
        return "verify-login-otp";
    }

    @PostMapping("/verify-login-otp")
    public String verifyLoginOtp(@RequestParam String otp,
                                 HttpSession session,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        String username = (String) session.getAttribute("pendingLoginUsername");
        String role     = (String) session.getAttribute("pendingLoginRole");

        if (username == null) return "redirect:/login";

        if (!otpService.validateOtp("login:" + username, otp.trim())) {
            User user = userService.findByUsername(username);
            model.addAttribute("maskedEmail", maskEmail(user != null ? user.getEmail() : ""));
            model.addAttribute("error", "Invalid or expired OTP. Please try again.");
            return "verify-login-otp";
        }

        // OTP valid — authenticate the user with full UserDetails as principal
        UserDetails userDetails = userService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Send welcome notification async
        User user = userService.findByUsername(username);
        if (user != null && user.getEmail() != null) {
            emailService.sendLoginNotification(user.getEmail(), user.getFullName());
        }

        session.removeAttribute("pendingLoginUsername");
        session.removeAttribute("pendingLoginRole");

        boolean isAdmin = userDetails.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return "redirect:" + (isAdmin ? "/admin/dashboard" : "/");
    }

    @PostMapping("/resend-login-otp")
    public String resendLoginOtp(HttpSession session, RedirectAttributes redirectAttributes) {
        String username = (String) session.getAttribute("pendingLoginUsername");
        if (username == null) return "redirect:/login";

        User user = userService.findByUsername(username);
        if (user != null && user.getEmail() != null) {
            String otp = otpService.generateOtp("login:" + username);
            emailService.sendLoginOtp(user.getEmail(), user.getFullName(), otp);
            redirectAttributes.addFlashAttribute("message", "A new OTP has been sent to your email.");
        }
        return "redirect:/verify-login-otp";
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "your email";
        String[] parts = email.split("@");
        String local = parts[0];
        String domain = parts[1];
        if (local.length() <= 2) return local.charAt(0) + "***@" + domain;
        return local.charAt(0) + "***" + local.charAt(local.length() - 1) + "@" + domain;
    }
}
