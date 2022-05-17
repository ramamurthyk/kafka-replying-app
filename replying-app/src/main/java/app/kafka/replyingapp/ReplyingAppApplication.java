package app.kafka.replyingapp;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
@EntityScan("app.kafka.replyingapp.voucher")
public class ReplyingAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReplyingAppApplication.class, args);
	}

	@Bean
	@Profile("default")
	public ApplicationRunner runner() {
		return args -> {
			System.out.println("Running");
		};
	}

}
