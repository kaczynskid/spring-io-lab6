package com.example;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
public class ReservationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationServiceApplication.class, args);
	}
}

@Slf4j
@RestController
@RequestMapping("/reservations")
class ReservationController {

	@RequestMapping(method = GET)
	public List<Reservation> listReservations() {
        log.info("Listing reservations...");
		return Arrays.stream("Jarek,Piotr,Marek,Mateusz,Maciek,Piotr".split(","))
			.map(Reservation::new).collect(Collectors.toList());
	}
}

@NoArgsConstructor
@AllArgsConstructor
@Data
class Reservation {

	String name;
}
