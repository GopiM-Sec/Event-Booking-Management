package com.eventbooking.config;

import com.eventbooking.model.User;
import com.eventbooking.service.EmailService;
import com.eventbooking.service.OtpService;
import com.eventbooking.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;

    @Autowired
    private OtpService otpService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String username = authentication.getName();
        User user = userService.findByUsername(username);

        if (user != null && user.getEmail() != null && !user.getEmail().isBlank()) {
            // Generate and send OTP
            String otp = otpService.generateOtp("login:" + username);
            emailService.sendLoginOtp(user.getEmail(), user.getFullName(), otp);

            // Store pending auth in session — clear Spring Security context until OTP verified
            HttpSession session = request.getSession();
            session.setAttribute("pendingLoginUsername", username);
            session.setAttribute("pendingLoginRole", authentication.getAuthorities().iterator().next().getAuthority());

            // Clear the security context so user is NOT yet authenticated
            SecurityContextHolder.clearContext();

            response.sendRedirect("/verify-login-otp");
        } else {
            // No email on record — skip OTP, log in directly
            boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            response.sendRedirect(isAdmin ? "/admin/dashboard" : "/");
        }
    }
}
