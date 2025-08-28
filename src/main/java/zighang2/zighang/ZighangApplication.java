package zighang2.zighang;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ZighangApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZighangApplication.class, args);
	}

}
