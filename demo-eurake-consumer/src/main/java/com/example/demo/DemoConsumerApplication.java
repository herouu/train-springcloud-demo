package com.example.demo;

import java.util.List;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class DemoConsumerApplication {


  public static void main(String[] args) {
    SpringApplication.run(DemoConsumerApplication.class, args);
  }
}

@FeignClient("eureka-produce-demo")
interface FeignService {

  @RequestMapping(value = "/produce")
  List<Object> getUsers( );

}
