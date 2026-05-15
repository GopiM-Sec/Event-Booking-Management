package com.eventbooking.controller;

import com.eventbooking.model.Booking;
import com.eventbooking.model.Event;
import com.eventbooking.service.BookingService;
import com.eventbooking.service.EmailService;
import com.eventbooking.service.OtpService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class EventController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/")
    public String showHomePage(Model model) {
        model.addAttribute("events", bookingService.getAllEvents());
        return "index";
    }

    @GetMapping("/event/{id}")
    public String viewEventDetails(@PathVariable Long id, Model model) {
        Event event = bookingService.getEventById(id);
        if (event == null) return "redirect:/";
        model.addAttribute("event", event);
        return "event-details";
    }

    @GetMapping("/booking")
    public String showBookingForm(@RequestParam(required = false) Long eventId, Model model) {
        if (eventId == null) return "redirect:/";
        Event event = bookingService.getEventById(eventId);
        if (event == null) return "redirect:/";
        model.addAttribute("booking", new Booking());
        model.addAttribute("event", event);
        return "booking";
    }

    // Step 1: User submits booking form → send OTP → redirect to OTP page
    @PostMapping("/book")
    public String initiateBooking(@RequestParam Long eventId,
                                  @ModelAttribute Booking booking,
                                  HttpSession session,
                                  Model model) {
        Event event = bookingService.getEventById(eventId);
        if (event == null) return "redirect:/";

        // Basic validation before sending OTP
        if (booking.getNumberOfTickets() <= 0) {
            model.addAttribute("error", "Number of tickets must be positive.");
            model.addAttribute("event", event);
            return "booking";
        }
        if (booking.getNumberOfTickets() > event.getAvailableTickets()) {
            model.addAttribute("error", "Not enough tickets. Only " + event.getAvailableTickets() + " remaining.");
            model.addAttribute("event", event);
            return "booking";
        }

        // Store booking details in session pending OTP
        session.setAttribute("pendingBooking", booking);
        session.setAttribute("pendingEventId", eventId);

        // Generate and send OTP
        String otpKey = "booking:" + booking.getEmail();
        String otp = otpService.generateOtp(otpKey);
        emailService.sendBookingOtp(booking.getEmail(), booking.getUserName(), event.getName(), otp);

        return "redirect:/verify-booking-otp";
    }

    // Step 2: Show OTP verification page
    @GetMapping("/verify-booking-otp")
    public String showBookingOtpPage(HttpSession session, Model model) {
        Booking booking = (Booking) session.getAttribute("pendingBooking");
        if (booking == null) return "redirect:/";

        model.addAttribute("maskedEmail", maskEmail(booking.getEmail()));
        return "verify-booking-otp";
    }

    // Step 3: Verify OTP → complete booking
    @PostMapping("/verify-booking-otp")
    public String verifyBookingOtp(@RequestParam String otp,
                                   HttpSession session,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        Booking booking = (Booking) session.getAttribute("pendingBooking");
        Long eventId    = (Long) session.getAttribute("pendingEventId");

        if (booking == null || eventId == null) return "redirect:/";

        String otpKey = "booking:" + booking.getEmail();

        if (!otpService.validateOtp(otpKey, otp.trim())) {
            model.addAttribute("maskedEmail", maskEmail(booking.getEmail()));
            model.addAttribute("error", "Invalid or expired OTP. Please try again.");
            return "verify-booking-otp";
        }

        // OTP valid — process the booking
        String result = bookingService.bookTickets(eventId, booking);

        if ("SUCCESS".equals(result)) {
            session.removeAttribute("pendingBooking");
            session.removeAttribute("pendingEventId");
            redirectAttributes.addFlashAttribute("bookingConfirmed", booking);
            redirectAttributes.addFlashAttribute("remainingTickets",
                bookingService.getEventById(eventId).getAvailableTickets());
            return "redirect:/confirmation";
        } else {
            model.addAttribute("maskedEmail", maskEmail(booking.getEmail()));
            model.addAttribute("error", result);
            return "verify-booking-otp";
        }
    }

    @PostMapping("/resend-booking-otp")
    public String resendBookingOtp(HttpSession session, RedirectAttributes redirectAttributes) {
        Booking booking = (Booking) session.getAttribute("pendingBooking");
        Long eventId    = (Long) session.getAttribute("pendingEventId");

        if (booking == null || eventId == null) return "redirect:/";

        Event event = bookingService.getEventById(eventId);
        String otpKey = "booking:" + booking.getEmail();
        String otp = otpService.generateOtp(otpKey);
        emailService.sendBookingOtp(booking.getEmail(), booking.getUserName(),
            event != null ? event.getName() : "Event", otp);

        redirectAttributes.addFlashAttribute("message", "A new OTP has been sent to your email.");
        return "redirect:/verify-booking-otp";
    }

    @GetMapping("/confirmation")
    public String showConfirmationPage(Model model) {
        if (!model.containsAttribute("bookingConfirmed")) return "redirect:/";
        return "confirmation";
    }

    @GetMapping("/admin/dashboard")
    public String showAdminDashboard(Model model) {
        model.addAttribute("bookings", bookingService.getAllBookings());
        model.addAttribute("events", bookingService.getAllEvents());
        model.addAttribute("newEvent", new Event());
        return "dashboard";
    }

    @PostMapping("/admin/event/add")
    public String addEvent(@ModelAttribute Event event, RedirectAttributes redirectAttributes) {
        bookingService.saveEvent(event);
        redirectAttributes.addFlashAttribute("message", "Event added successfully!");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/admin/event/delete/{id}")
    public String deleteEvent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        bookingService.deleteEvent(id);
        redirectAttributes.addFlashAttribute("message", "Event deleted successfully!");
        return "redirect:/admin/dashboard";
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "your email";
        String[] parts = email.split("@");
        String local = parts[0];
        String domain = parts[1];
        if (local.length() <= 2) return local.charAt(0) + "***@" + domain;
        return local.charAt(0) + "***" + local.charAt(local.length() - 1) + "@" + domain;
    }
}
