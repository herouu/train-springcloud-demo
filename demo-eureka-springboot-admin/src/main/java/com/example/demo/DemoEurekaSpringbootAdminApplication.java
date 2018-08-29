package com.example.demo;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
@EnableDiscoveryClient
@EnableAdminServer
public class DemoEurekaSpringbootAdminApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoEurekaSpringbootAdminApplication.class, args);
	}
}
