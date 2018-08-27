package com.example.demo;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import javax.annotation.Resource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableDiscoveryClient
@EnableEurekaClient
@EnableHystrix
@EnableHystrixDashboard
@EnableCircuitBreaker
public class DemoEurekaConsumerFinal2Application {

  /**
   * http://localhost:12004/actuator/hystrix.stream
   * @param args
   */
  public static void main(String[] args) {
    SpringApplication.run(DemoEurekaConsumerFinal2Application.class, args);
  }
}

@RestController
class Final2Controller {

  @Resource
  private Final2Service final2Service;
  @GetMapping("/final-two/consumer")
  public String consumer() {
    return "this is /final-two/consumer";
  }

  @GetMapping("/final-two/fallback")
  public String fallbackFinal2() {
    return final2Service.finalConsumer();
  }

}

@Service
class Final2Service {

  @HystrixCommand(fallbackMethod = "errorFinalConsumer")
  public String finalConsumer() {
    return "final2 consumer";
  }

  public String errorFinalConsumer() {
    return "final consumer hasError!";
  }
}


