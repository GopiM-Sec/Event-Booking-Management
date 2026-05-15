package com.eventbooking.service;

import com.eventbooking.model.Booking;
import com.eventbooking.model.Event;
import com.eventbooking.model.User;
import com.eventbooking.repository.BookingRepository;
import com.eventbooking.repository.EventRepository;
import com.eventbooking.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Lazy;

import java.util.List;

@Service
public class BookingService {
    
    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void seedDatabase() {
        if (eventRepository.count() == 0) {
            eventRepository.save(new Event(
                "Annual Tech Symposium 2024",
                "Information Technology",
                "2024-12-15",
                "10:00 AM",
                "Main Auditorium Hall A",
                250.0,
                50,
                "A full-day event covering the latest in AI, Cloud Computing, and Developer Experience."
            ));
            eventRepository.save(new Event(
                "HR Wellness Workshop",
                "Human Resources",
                "2024-05-20",
                "02:00 PM",
                "Conference Room 2",
                0.0,
                30,
                "Focusing on mental health and work-life balance for all department members."
            ));
            eventRepository.save(new Event(
                "Finance Year End Gala",
                "Finance",
                "2024-06-30",
                "07:30 PM",
                "Hotel Grand Regency",
                1500.0,
                100,
                "Celebrating the end of a successful fiscal year with dinner and awards."
            ));
        }

        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("System Administrator");
            admin.setEmail("****@veltech.edu.in");
            admin.setRole("ROLE_ADMIN");
            userRepository.save(admin);
        }
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id).orElse(null);
    }

    @Transactional
    public String bookTickets(Long eventId, Booking booking) {
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) return "Event not found.";
        
        if (booking.getNumberOfTickets() <= 0) {
            return "Number of tickets must be positive.";
        }
        if (booking.getNumberOfTickets() > event.getAvailableTickets()) {
            return "Not enough tickets available. Only " + event.getAvailableTickets() + " remaining.";
        }

        // Update event tickets
        event.setAvailableTickets(event.getAvailableTickets() - booking.getNumberOfTickets());
        eventRepository.save(event);
        
        // Finalize booking
        booking.setEvent(event);
        booking.setEventName(event.getName());
        booking.setTotalAmount(booking.getNumberOfTickets() * event.getTicketPrice());
        bookingRepository.save(booking);

        // Send confirmation email asynchronously
        if (booking.getEmail() != null && !booking.getEmail().isBlank()) {
            emailService.sendBookingConfirmation(booking);
        }

        return "SUCCESS";
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Transactional
    public void saveEvent(Event event) {
        if (event.getId() == null) {
            event.setTotalTickets(event.getAvailableTickets());
        }
        eventRepository.save(event);
    }

    @Transactional
    public void deleteEvent(Long id) {
        eventRepository.deleteById(id);
    }
}
