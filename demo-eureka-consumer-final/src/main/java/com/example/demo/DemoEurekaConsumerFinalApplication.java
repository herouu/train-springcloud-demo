package com.example.demo;


import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableDiscoveryClient
@EnableEurekaClient
@EnableHystrix
@EnableHystrixDashboard
@EnableCircuitBreaker
public class DemoEurekaConsumerFinalApplication {

  @Bean
  @LoadBalanced
  RestTemplate restTemplate() {
    return new RestTemplate();
  }

  /**
   * http://localhost:12003/actuator/hystrix.stream
   * @param args
   */
  public static void main(String[] args) {
    SpringApplication.run(DemoEurekaConsumerFinalApplication.class, args);
  }

}

@RestController
class FinalConsumerController {

  @Autowired
  ConsumerService consumerService;

  @GetMapping("/final")
  public List<String> getFinalConsumer() {
    String s = consumerService.finalConsuomer();
    ArrayList<String> result = new ArrayList<>();
    result.add(s);
    result.add("final is over!");
    return result;
  }

  @GetMapping("/final-one/consumer")
  public String consumer() {
    return "this is /final-one/consumer";
  }


}

@Service
class ConsumerService {

  @Autowired
  RestTemplate restTemplate;

  @HystrixCommand(fallbackMethod = "errorFinalConsumer")
  public String finalConsuomer() {
    return restTemplate.getForObject("http://EUREKA-CONSUMER-DEMO/consumer", String.class);
  }

  public String errorFinalConsumer() {
    return "final consumer hasError!";
  }
}



