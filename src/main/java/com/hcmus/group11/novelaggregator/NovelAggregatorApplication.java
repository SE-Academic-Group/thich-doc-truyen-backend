package com.hcmus.group11.novelaggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;


@OpenAPIDefinition(
		info = @Info(
				title = "Spring Boot NOVEL AGGREGATOR Application REST API",
				version = "1.0",
				description = "Spring Boot NOVEL AGGREGATOR Application REST API"
		),
		servers = {
				@Server(
						url = "http://localhost:8080",
						description = "Local server"
				),
		},
		externalDocs = @ExternalDocumentation(
				description = "Spring Boot NOVEL AGGREGATOR Application REST API Source Code",
				url = "https://github.com/nguyenphucphat/sd-project-1"
		)
)
@SpringBootApplication
public class NovelAggregatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(NovelAggregatorApplication.class, args);
	}

}
