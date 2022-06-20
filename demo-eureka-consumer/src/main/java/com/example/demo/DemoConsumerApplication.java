package com.example.demo;

import cn.hutool.extra.spring.SpringUtil;
import feign.Logger;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@FeignClient(value = "eureka-produce-demo")
interface FeignService {

    @RequestMapping(value = "/produce")
    ResponseEntity<byte[]> getUsers();

}

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@Import(SpringUtil.class)
public class DemoConsumerApplication {

    @Autowired
    FF4jInterceptor ff4jInterceptor;

    public static void main(String[] args) {
        SpringApplication.run(DemoConsumerApplication.class, args);
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.HEADERS;
    }

    @Bean
    public FF4j ff4j() {
        return (FF4j) Enhancer.create(FF4j.class, ff4jInterceptor);
    }
}

@RestController
class ConsumerController {

    @Autowired
    FeignService feignService;

    @Autowired
    LoadBalancerClient loadBalancerClient;

    @Autowired
    FF4j ff4j;

    @GetMapping("/consumer")
    public Object getUserList() {
        long l = System.currentTimeMillis();
        ResponseEntity<byte[]> user = feignService.getUsers();
        System.out.println(System.currentTimeMillis() - l);
        return user;
    }


    @GetMapping("/loadBalance")
    public Object loadBalance() {
        ServiceInstance choose = loadBalancerClient.choose("eureka-produce-demo");
        return choose;
    }

    @GetMapping("/ff4j")
    public Object ff4j() {
        return ff4j.check("first");
    }


    @GetMapping("/changeNull")
    public void chooseNull(Boolean flag) {
        if (flag) {
            FF4jInterceptor.chooseNull = true;
            return;
        }
        FF4jInterceptor.chooseNull = false;
    }
}
