# Running the Project in Eclipse IDE

Follow these steps to import and run the **DepartmentEventTicketBooking** project.

## Prerequisites

- **JDK 17** or higher installed
- **Eclipse IDE** (preferably *Eclipse IDE for Enterprise Java and Web Developers*)
- **Maven** (usually bundled with Eclipse)

## Setup

### 1. Configure Application Properties

Copy the example config and fill in your own values:

```
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Then open `application.properties` and set:

- `spring.mail.username` — your Gmail address
- `spring.mail.password` — your [Gmail App Password](https://myaccount.google.com/apppasswords)
- `groq.api.key` — your [Groq API key](https://console.groq.com/keys)

> **Note:** `application.properties` is listed in `.gitignore` and will never be committed.

### 2. Import the Project in Eclipse

1. Go to `File` > `Import...`
2. Select `Maven` > `Existing Maven Projects` and click `Next`
3. Browse to the directory where you cloned/extracted the project
4. Ensure `pom.xml` is selected and click `Finish`
5. Wait for Eclipse to download all Maven dependencies (progress shown in the bottom-right corner)

### 3. Run the Application

- Right-click the project in Project Explorer → `Run As` → `Spring Boot App`
- Or navigate to `src/main/java/com/eventbooking/DepartmentEventTicketBookingApplication.java`, right-click → `Run As` → `Java Application`

### 4. Access the Application

Open your browser and go to: [http://localhost:8080](http://localhost:8080)

## Project Structure

```
src/main/java/com/eventbooking/
├── config/         # Security configuration
├── controller/     # MVC controllers
├── model/          # JPA entities
├── repository/     # Spring Data repositories
└── service/        # Business logic

src/main/resources/
├── templates/      # Thymeleaf HTML pages
├── static/         # CSS and JS files
├── application.properties.example   # Template config (safe to commit)
└── application.properties           # Your local config (gitignored)
```

## Troubleshooting

- **Red errors in code:** Right-click project → `Maven` → `Update Project...` → check `Force Update of Snapshots/Releases` → `OK`
- **Port conflict:** Ensure nothing else is running on port `8080`
- **Email not sending:** Make sure you're using a Gmail App Password, not your regular Gmail password
