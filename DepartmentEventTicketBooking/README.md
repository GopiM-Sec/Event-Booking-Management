# Department Event Ticket Booking System

A Spring Boot web application for managing and booking tickets for internal department events. Features include user registration with OTP verification, event browsing, ticket booking, email confirmations, and an AI-powered chatbot.

## Features

- User registration and login with OTP-based email verification
- Browse and view department events
- Book tickets with confirmation emails
- AI chatbot powered by Groq API
- Spring Security for authentication and authorization

## Tech Stack

- **Backend:** Java 21, Spring Boot 3.2, Spring Security, Spring Data JPA
- **Frontend:** Thymeleaf, HTML/CSS/JS
- **Database:** H2 (in-memory, development)
- **Email:** JavaMail (Gmail SMTP)
- **AI:** Groq API

## Getting Started

See [INSTRUCTIONS.md](INSTRUCTIONS.md) for full setup and run instructions.

### Quick Start

1. Clone the repository
2. Copy the config template:
   ```bash
   cp src/main/resources/application.properties.example src/main/resources/application.properties
   ```
3. Fill in your credentials in `application.properties`
4. Import into Eclipse as a Maven project and run as Spring Boot App

## Configuration

All sensitive values are kept in `application.properties` (gitignored). Use `application.properties.example` as a reference.

| Property | Description |
|---|---|
| `spring.mail.username` | Your Gmail address |
| `spring.mail.password` | Gmail App Password |
| `groq.api.key` | Groq API key |
