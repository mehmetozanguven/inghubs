package com.mehmetozanguven.inghubs_digital_wallet;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulith;

@OpenAPIDefinition
@SpringBootApplication
@Modulith(sharedModules = {"core"})
public class InghubsDigitalWalletApplication {

	public static void main(String[] args) {
		SpringApplication.run(InghubsDigitalWalletApplication.class, args);
	}

}
