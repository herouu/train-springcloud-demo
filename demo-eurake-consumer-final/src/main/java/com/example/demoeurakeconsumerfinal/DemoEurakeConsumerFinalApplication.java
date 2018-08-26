package com.example.demoeurakeconsumerfinal;

import java.util.List;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class DemoEurakeConsumerFinalApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoEurakeConsumerFinalApplication.class, args);
	}

}

@FeignClient("eureka-consumer-demo")
interface FinalService{
	@GetMapping("/consumer")
	List<Object> finalConsumer();
}
