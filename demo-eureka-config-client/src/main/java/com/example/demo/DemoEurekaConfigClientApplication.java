package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class DemoEurekaConfigClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoEurekaConfigClientApplication.class, args);
	}
}
@RestController
@RefreshScope
class ConfigClientController{
	@Value("${config-demo}")
	 String configDemo;

	@GetMapping(value = "/getConfigDemo")
	public String getConfigDemo(){
		return configDemo;
	}
}
