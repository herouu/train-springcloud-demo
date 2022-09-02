package com.example.demo;

import brave.sampler.Sampler;
import lombok.Data;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableDiscoveryClient
public class DemoProduceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoProduceApplication.class, args);
    }

    @Bean
    public Sampler defaultSampler() {
        return Sampler.ALWAYS_SAMPLE;
    }
}

@RestController
class ProduceController {

    @GetMapping("/produce")
    public List<User> userList() {
        return createUsers();
    }

    private List<User> createUsers() {
        long l = System.currentTimeMillis();
        ArrayList<User> o = new ArrayList<>();
        for (int i = 0; i <= 1000000; i++) {
            User user = new User();
            user.setPhone("1388948577" + i);
            user.setUsername("zhangsan" + i);
            user.setAddress("xianggang" + i);
            user.setNo("what" + i);
            o.add(user);
        }
//        try {
//            Thread.sleep(7000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        System.out.println(System.currentTimeMillis() - l);
        return o;
    }

    @Data
    class User {
        private String address;
        private String username;
        private String phone;
        private String no;
    }
}