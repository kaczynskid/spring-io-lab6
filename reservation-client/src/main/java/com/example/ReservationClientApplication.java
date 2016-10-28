package com.example;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableDiscoveryClient
public class ReservationClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationClientApplication.class, args);
	}
}

@RestController
@RequestMapping("/reservations")
class ReservationsController {

    private final DiscoveryClient discovery;
    private final RestTemplate rest;

    public ReservationsController(DiscoveryClient discovery) {
        this.discovery = discovery;
        this.rest = new RestTemplate();
    }

    @RequestMapping(path = "/{name}", method = GET)
    ResponseEntity<Reservation> gteReservation(@PathVariable("name") String name) {
        return rest.getForEntity(
            url().toString() + "/custom-reservations/" + name,
            Reservation.class);
    }

    private URI url() {
        return discovery.getInstances("reservationservice").stream()
            .findFirst().map(ServiceInstance::getUri)
            .orElseThrow(NoReservationsServiceAvailable::new);
    }
}

class NoReservationsServiceAvailable extends RuntimeException {
}

@NoArgsConstructor
@AllArgsConstructor
@Data
class Reservation {

    String name;
}

@Slf4j
@Component
class DiscoveryClientExample implements ApplicationRunner {

	private final DiscoveryClient discoveryClient;

	@Autowired
	public DiscoveryClientExample(DiscoveryClient discoveryClient) {
		this.discoveryClient = discoveryClient;
	}

    @Override
    public void run(ApplicationArguments args) throws Exception {
		try {
			log.info("------------------------------");
			log.info("DiscoveryClient Example");

			discoveryClient.getInstances("reservationservice").forEach(instance -> {
				log.info("Reservation service: ");
				log.info("  ID: {}", instance.getServiceId());
				log.info("  URI: {}", instance.getUri());
				log.info("  Meta: {}", instance.getMetadata());
			});

			log.info("------------------------------");
		} catch (Exception e) {
			log.error("DiscoveryClient Example Error!", e);
		}
	}
}
