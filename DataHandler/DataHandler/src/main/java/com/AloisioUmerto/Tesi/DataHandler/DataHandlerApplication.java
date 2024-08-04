package com.AloisioUmerto.Tesi.DataHandler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DataHandlerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataHandlerApplication.class, args);
		System.out.println("Swagger UI: http://localhost:8080/swagger-ui/index.html");

	}

}
