package com.example.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class DemoConsumerApplication {


  public static void main(String[] args) {
    SpringApplication.run(DemoConsumerApplication.class, args);
  }
}

@FeignClient(value = "eureka-produce-demo",fallback = HystricFeignService.class)
interface FeignService {

  @RequestMapping(value = "/produce")
  List<Object> getUsers( );

}
@Component
class HystricFeignService implements FeignService{

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

  @GetMapping("/consumer")
  public Object getUserList() {
    List<Object> users = feignService.getUsers();
    HashMap<Object, Object> map = new HashMap<>();
    map.put("username", "consumer1");
    map.put("phone", "13000000001");
    users.add(map);
    return users;
  }
}
