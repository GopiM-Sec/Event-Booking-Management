package com.eventbooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DepartmentEventTicketBookingApplication {

	public static void main(String[] args) {
		SpringApplication.run(DepartmentEventTicketBookingApplication.class, args);
	}

}
