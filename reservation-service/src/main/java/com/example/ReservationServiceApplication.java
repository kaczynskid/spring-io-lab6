package com.example;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterDelete;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
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
@Component
@RepositoryEventHandler
class ReservationEventHandler {

    private final CounterService counter;

    public ReservationEventHandler(CounterService counter) {
        this.counter = counter;
    }

    @HandleAfterCreate
    public void create(Reservation reservation) {
        log.info("Created redervation for {}.", reservation.name);
        counter.increment("count");
        counter.increment("create");
    }

    @HandleAfterSave
    public void save(Reservation reservation) {
        log.info("Updated redervation for {}.", reservation.name);
        counter.increment("save");
    }

    @HandleAfterDelete
    public void delete(Reservation reservation) {
        log.info("Removed redervation for {}.", reservation.name);
        counter.decrement("count");
        counter.increment("delete");
    }
}

@Component
class ReservationHealth implements HealthIndicator {

    @Override
    public Health health() {
        return Health.status("This app is UP").build();
    }
}

@Component
class ReservationInfo implements InfoContributor {

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("currentTime", currentTimeMillis()).build();
    }
}

@Component
class ReservationResourceProcessor implements ResourceProcessor<Resource<Reservation>> {

    @Override
    public Resource<Reservation> process(Resource<Reservation> resource) {
        Reservation reservation = resource.getContent();
        String url = format("https://www.google.pl/search?tbm=isch&q=%s", reservation.getName());
        resource.add(new Link(url, "photo"));
        return resource;
    }
}

@Slf4j
@Component
class ReservationsInitializer implements ApplicationRunner {

    private final ReservationRepository reservations;
    private final CounterService counter;
    private final Environment environment;

    public ReservationsInitializer(ReservationRepository reservations,
                                   CounterService counter,
                                   Environment environment) {
        this.reservations = reservations;
        this.counter = counter;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Starting initialization...");
        Arrays.stream("Jarek,Piotr,Marek,Mateusz,Maciek".split(","))
            .map(Reservation::new)
            .forEach(reservation -> {
                log.info("Saving {}...", reservation.name);
                reservations.save(new Reservation(reservation.name + " " + environment.getProperty("info.instanceId")));
                counter.increment("count");
            });
        log.info("Initialization done.");
    }
}

@Slf4j
@RestController
@RequestMapping("/custom-reservations")
class ReservationController {

    private final ReservationRepository reservations;

    public ReservationController(ReservationRepository reservations) {
        this.reservations = reservations;
    }

    @RequestMapping(method = GET)
	public Collection<Reservation> list() {
        return reservations.findAll();
	}

    @RequestMapping(method = POST)
	public ResponseEntity<Void> create(@RequestBody Reservation reservation) {
        if (reservations.findByName(reservation.name) != null) {
            throw new ReservationAlreadyExists();
        }

        reservations.save(reservation);

        return ResponseEntity.created(selfURI(reservation)).build();
    }

    private URI selfURI(Reservation reservation) {
        return ControllerLinkBuilder.linkTo(
            ControllerLinkBuilder.methodOn(ReservationController.class).get(reservation.name))
            .toUri();
    }

    @RequestMapping(path = "/{name}", method = GET)
    public ResponseEntity<?> get(@PathVariable("name") String name) {
        Reservation reservation = reservations.findByName(name);
        if (reservation != null) {
            return ResponseEntity.ok()
                .header("X-Reservation-Grats", "Well done!")
                .body(reservation);
        }
        return ResponseEntity.notFound()
            .header("X-Reservation-Apology", "Sorry man ;)").build();
    }

    @RequestMapping(path = "/{name}", method = DELETE)
    @ResponseStatus(NO_CONTENT)
    public void delete(@PathVariable("name") String name) {
        Reservation reservation = reservations.findByName(name);
        if (reservation != null) {
            reservations.delete(reservation);
        }
    }
}

@ResponseStatus(value = BAD_REQUEST, reason = "Already reserved!")
class ReservationAlreadyExists extends RuntimeException {
}

@RepositoryRestResource
interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @RestResource(path = "by-name", rel = "find-by-name")
    Reservation findByName(@Param("name")String name);

    @RestResource(exported = false)
    @Override
    void delete(Long id);
}

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = "name")
})
class Reservation {

    @Id
    @GeneratedValue
    Long id;

	String name;

    Reservation(String name) {
        this.name = name;
    }
}
