package com.example;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
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
@EnableFeignClients
public class ReservationClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationClientApplication.class, args);
	}

    @Bean @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

@FeignClient("reservationservice")
interface ReservationsClient {

    @RequestMapping(path = "/reservations", method = GET)
    Resources<Reservation> listReservations();
}

@Slf4j
@RestController
@RequestMapping("/reservations")
class ReservationsController {

    private final DiscoveryClient discovery;
    private final RestTemplate rest;

    public ReservationsController(DiscoveryClient discovery, RestTemplate rest) {
        this.discovery = discovery;
        this.rest = rest;
    }

    @RequestMapping(path = "/byName/{name}", method = GET)
    ResponseEntity<Reservation> getReservationByName(@PathVariable("name") String name) {
        return new RestTemplate().getForEntity(
            url().toString() + "/custom-reservations/" + name,
            Reservation.class);
    }

    private URI url() {
        return discovery.getInstances("reservationservice").stream()
            .findFirst().map(ServiceInstance::getUri)
            .orElseThrow(NoReservationsServiceAvailable::new);
    }

    @RequestMapping(path = "/byId/{id}", method = GET)
    ResponseEntity<Resource<Reservation>> getReservationById(@PathVariable("id") Long id) {
        // FIXME marshaling !!!
        ParameterizedTypeReference<Resource<Reservation>> responseType =
            new ParameterizedTypeReference<Resource<Reservation>>() { };
        return rest.exchange("http://reservationservice/reservations/{id}", HttpMethod.GET, null, responseType, id);
    }

    @RequestMapping(path = "/names", method = GET)
    public List<String> names() {
        log.info("Calling names...");

        ParameterizedTypeReference<Resources<Reservation>> responseType =
            new ParameterizedTypeReference<Resources<Reservation>>() {};

        ResponseEntity<Resources<Reservation>> exchange =
            rest.exchange("http://reservationservice/reservations", HttpMethod.GET, null, responseType);

        return exchange.getBody().getContent().stream()
            .map(Reservation::getName)
            .collect(Collectors.toList());
    }

    @Autowired
    private ReservationsClient feignClient;

    @RequestMapping(path = "/feign-names", method = GET)
    public List<String> feignNames() {
        log.info("Calling feign-names...");
        return feignClient.listReservations().getContent().stream()
            .map(Reservation::getName).collect(Collectors.toList());
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
