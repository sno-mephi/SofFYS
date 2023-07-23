package ru.idfedorov09.telegram.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SnoBotHandlerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SnoBotHandlerApplication.class, args);
	}

}
