package pl.deltavista;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}

@RestController
@EnableConfigurationProperties(DemoProperties.class)
class MojPierwszyMikroSerwis {

	@Value("${demo.greet:hello}")
	private String greet;

	@Autowired
	private DemoProperties properties;

	@RequestMapping("/{name}")
	MyMessage hello(@PathVariable("name") String name,
					@RequestParam(name = "greet", defaultValue = "Hello") String greet) {
		return new MyMessage(greet + " " + name);
	}
}

@ConfigurationProperties(prefix = "demo")
class DemoProperties {

	String greet;

}

class MyMessage {

	private String message;

	public MyMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
