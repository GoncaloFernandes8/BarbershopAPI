package barbershopAPI.barbershopAPI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BarbershopApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(BarbershopApiApplication.class, args);
	}

}
