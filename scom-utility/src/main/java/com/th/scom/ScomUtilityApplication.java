package com.th.scom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
	    info = @Info(
	        title = "Payment API",
	        version = "1.0",
	        description = "API for generating payment redirect URLs"
	    )
	)
@SpringBootApplication
public class ScomUtilityApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScomUtilityApplication.class, args);
	}

}
