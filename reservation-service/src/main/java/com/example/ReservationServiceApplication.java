package com.example;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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

    final Map<String, Reservation> reservations = new HashMap<>();

    public ReservationController() {
        Arrays.stream("Jarek,Piotr,Marek,Mateusz,Maciek".split(","))
            .map(Reservation::new)
            .forEach(reservation -> reservations.put(reservation.name, reservation));
    }

    @RequestMapping(method = GET)
	public Collection<Reservation> list() {
        return reservations.values();
	}

    @RequestMapping(method = POST)
	public void create(@RequestBody Reservation reservation) {
        if (reservations.containsKey(reservation.name)) {
            throw new ReservationAlreadyExists();
        }

        reservations.put(reservation.name, reservation);
    }

    @RequestMapping(path = "/{name}", method = GET)
    public ResponseEntity<?> get(@PathVariable("name") String name) {
        if (reservations.containsKey(name)) {
            return ResponseEntity.ok()
                .header("X-Reservation-Grats", "Well done!")
                .body(reservations.get(name));
        }
        return ResponseEntity.notFound()
            .header("X-Reservation-Apology", "Sorry man ;)").build();
    }

    @RequestMapping(path = "/{name}", method = DELETE)
    @ResponseStatus(NO_CONTENT)
    public void delete(@PathVariable("name") String name) {
        reservations.remove(name);
    }
}

@ResponseStatus(value = BAD_REQUEST, reason = "Already reserved!")
class ReservationAlreadyExists extends RuntimeException {
}

@NoArgsConstructor
@AllArgsConstructor
@Data
class Reservation {

	String name;
}
