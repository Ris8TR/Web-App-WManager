package com.myTesi.aloisioUmberto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebWApplication {
	public static void main(String[] args) {
		SpringApplication.run(WebWApplication.class, args);
		System.out.println("Swagger UI: http://192.168.15.34:8010/swagger-ui/index.html");
	}

}
