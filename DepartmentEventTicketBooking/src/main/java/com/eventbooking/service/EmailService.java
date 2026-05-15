package com.eventbooking.service;

import com.eventbooking.model.Booking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Sends a 6-digit OTP for login verification.
     */
    public void sendLoginOtp(String toEmail, String fullName, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Your EventSphere Login OTP");

            String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 30px; background: #f9fafb; border-radius: 12px;">
                    <div style="background: linear-gradient(135deg, #6366f1, #4f46e5); padding: 30px; border-radius: 10px; text-align: center;">
                        <h1 style="color: white; margin: 0; font-size: 28px;">EventSphere</h1>
                        <p style="color: rgba(255,255,255,0.85); margin: 8px 0 0;">Login Verification</p>
                    </div>
                    <div style="background: white; padding: 30px; border-radius: 10px; margin-top: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); text-align: center;">
                        <h2 style="color: #1e293b;">Hello, %s! 👋</h2>
                        <p style="color: #475569;">Use the OTP below to complete your login. It expires in <strong>5 minutes</strong>.</p>
                        <div style="font-size: 48px; font-weight: 900; letter-spacing: 12px; color: #6366f1; background: #eef2ff; padding: 20px 30px; border-radius: 12px; display: inline-block; margin: 20px 0;">%s</div>
                        <p style="color: #94a3b8; font-size: 13px; margin-top: 16px;">If you did not attempt to log in, please ignore this email.</p>
                    </div>
                </div>
                """.formatted(fullName != null ? fullName : "there", otp);

            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send login OTP to " + toEmail + ": " + e.getMessage());
        }
    }

    /**
     * Sends a 6-digit OTP for booking verification.
     */
    public void sendBookingOtp(String toEmail, String userName, String eventName, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Confirm Your Booking – OTP Required");

            String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 30px; background: #f9fafb; border-radius: 12px;">
                    <div style="background: linear-gradient(135deg, #6366f1, #4f46e5); padding: 30px; border-radius: 10px; text-align: center;">
                        <h1 style="color: white; margin: 0; font-size: 28px;">🎟 Booking Verification</h1>
                        <p style="color: rgba(255,255,255,0.85); margin: 8px 0 0;">EventSphere</p>
                    </div>
                    <div style="background: white; padding: 30px; border-radius: 10px; margin-top: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); text-align: center;">
                        <h2 style="color: #1e293b;">Hi %s!</h2>
                        <p style="color: #475569;">You're booking a ticket for <strong>%s</strong>.</p>
                        <p style="color: #475569;">Enter the OTP below to confirm. It expires in <strong>5 minutes</strong>.</p>
                        <div style="font-size: 48px; font-weight: 900; letter-spacing: 12px; color: #6366f1; background: #eef2ff; padding: 20px 30px; border-radius: 12px; display: inline-block; margin: 20px 0;">%s</div>
                        <p style="color: #94a3b8; font-size: 13px; margin-top: 16px;">If you did not request this, please ignore this email.</p>
                    </div>
                </div>
                """.formatted(userName, eventName, otp);

            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send booking OTP to " + toEmail + ": " + e.getMessage());
        }
    }

    /**
     * Sends a welcome email after successful login.
     */
    @Async
    public void sendLoginNotification(String toEmail, String fullName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Welcome back to EventSphere!");

            String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 30px; background: #f9fafb; border-radius: 12px;">
                    <div style="background: linear-gradient(135deg, #6366f1, #4f46e5); padding: 30px; border-radius: 10px; text-align: center;">
                        <h1 style="color: white; margin: 0; font-size: 28px;">EventSphere</h1>
                        <p style="color: rgba(255,255,255,0.85); margin: 8px 0 0;">Department Event Booking System</p>
                    </div>
                    <div style="background: white; padding: 30px; border-radius: 10px; margin-top: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.06);">
                        <h2 style="color: #1e293b;">Hello, %s! 👋</h2>
                        <p style="color: #475569; line-height: 1.6;">
                            You have successfully logged in to your EventSphere account.
                            Browse upcoming department events and book your tickets today!
                        </p>
                        <a href="http://localhost:8080" style="display: inline-block; margin-top: 20px; padding: 12px 28px; background: #6366f1; color: white; text-decoration: none; border-radius: 8px; font-weight: bold;">
                            Browse Events
                        </a>
                    </div>
                    <p style="text-align: center; color: #94a3b8; font-size: 12px; margin-top: 20px;">
                        If you did not log in, please contact support immediately.
                    </p>
                </div>
                """.formatted(fullName != null ? fullName : "there");

            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send login email to " + toEmail + ": " + e.getMessage());
        }
    }

    /**
     * Sends a booking confirmation email with ticket details.
     */
    @Async
    public void sendBookingConfirmation(Booking booking) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(booking.getEmail());
            helper.setSubject("Booking Confirmed – " + booking.getEventName());

            String amountDisplay = booking.getTotalAmount() == 0
                ? "FREE"
                : "₹" + String.format("%.2f", booking.getTotalAmount());

            String ticketId = "#EVT-" + (booking.getId() + 1000);

            String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 30px; background: #f9fafb; border-radius: 12px;">
                    <div style="background: linear-gradient(135deg, #6366f1, #4f46e5); padding: 30px; border-radius: 10px; text-align: center;">
                        <h1 style="color: white; margin: 0; font-size: 28px;">🎟 Booking Confirmed!</h1>
                        <p style="color: rgba(255,255,255,0.85); margin: 8px 0 0;">Ticket ID: %s</p>
                    </div>
                    <div style="background: white; padding: 30px; border-radius: 10px; margin-top: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.06);">
                        <h2 style="color: #1e293b; margin-top: 0;">%s</h2>
                        <table style="width: 100%%; border-collapse: collapse; margin-top: 16px;">
                            <tr style="border-bottom: 1px solid #e2e8f0;">
                                <td style="padding: 10px 0; color: #64748b; font-size: 14px;">Attendee</td>
                                <td style="padding: 10px 0; font-weight: 700; color: #1e293b; text-align: right;">%s</td>
                            </tr>
                            <tr style="border-bottom: 1px solid #e2e8f0;">
                                <td style="padding: 10px 0; color: #64748b; font-size: 14px;">Department</td>
                                <td style="padding: 10px 0; font-weight: 700; color: #1e293b; text-align: right;">%s</td>
                            </tr>
                            <tr style="border-bottom: 1px solid #e2e8f0;">
                                <td style="padding: 10px 0; color: #64748b; font-size: 14px;">Tickets</td>
                                <td style="padding: 10px 0; font-weight: 700; color: #1e293b; text-align: right;">%d</td>
                            </tr>
                            <tr>
                                <td style="padding: 10px 0; color: #64748b; font-size: 14px;">Amount Paid</td>
                                <td style="padding: 10px 0; font-weight: 700; color: #4f46e5; text-align: right; font-size: 18px;">%s</td>
                            </tr>
                        </table>
                        <a href="http://localhost:8080" style="display: inline-block; margin-top: 24px; padding: 12px 28px; background: #6366f1; color: white; text-decoration: none; border-radius: 8px; font-weight: bold;">
                            View More Events
                        </a>
                    </div>
                    <p style="text-align: center; color: #94a3b8; font-size: 12px; margin-top: 20px;">
                        Thank you for booking with EventSphere. See you at the event!
                    </p>
                </div>
                """.formatted(
                    ticketId,
                    booking.getEventName(),
                    booking.getUserName(),
                    booking.getDepartment(),
                    booking.getNumberOfTickets(),
                    amountDisplay
                );

            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send booking email to " + booking.getEmail() + ": " + e.getMessage());
        }
    }
}
