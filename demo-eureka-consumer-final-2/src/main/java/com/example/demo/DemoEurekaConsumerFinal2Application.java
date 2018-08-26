package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableEurekaClient
public class DemoEurekaConsumerFinal2Application {

  public static void main(String[] args) {
    SpringApplication.run(DemoEurekaConsumerFinal2Application.class, args);
  }
}

@RestController
class Final2Controller {
  @GetMapping("/final-two/consumer")
  public String consumer() {
    return "this is /final-two/consumer";
  }
}

