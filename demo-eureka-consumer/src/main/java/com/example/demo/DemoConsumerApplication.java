package com.example.demo;

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
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@FeignClient(value = "eureka-produce-demo", fallback = HystricFeignService.class)
interface FeignService {

    @RequestMapping(value = "/produce")
    List<Object> getUsers();

}

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class DemoConsumerApplication {


    public static void main(String[] args) {
        SpringApplication.run(DemoConsumerApplication.class, args);
    }


    @Bean
    public FF4j ff4j(LoadBalancerClient loadBalancerClient) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(FF4j.class);
        FF4j ff4j = new FF4j();
        FF4jInterceptor ff4jInterceptor = new FF4jInterceptor(ff4j, loadBalancerClient);
        return (FF4j) enhancer.create(FF4j.class, ff4jInterceptor);
    }
}

@Component
class HystricFeignService implements FeignService {

    @Override
    public List<Object> getUsers() {
        ArrayList<Object> userList = new ArrayList<>();
        userList.add("produce server is dead! consumer hystric is open!");
        return userList;
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
        List<Object> users = feignService.getUsers();
        HashMap<Object, Object> map = new HashMap<>();
        map.put("username", "consumer1");
        map.put("phone", "13000000001");
        users.add(map);
        return users;
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
}
