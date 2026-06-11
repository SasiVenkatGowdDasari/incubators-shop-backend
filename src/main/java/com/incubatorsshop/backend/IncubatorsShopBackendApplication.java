package com.incubatorsshop.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class IncubatorsShopBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(IncubatorsShopBackendApplication.class, args);
	}

}
