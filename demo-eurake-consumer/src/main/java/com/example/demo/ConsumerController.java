package com.example.demo;

import java.util.HashMap;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConsumerController {
    @Autowired
    FeignService feignService;

    @GetMapping("/consumer")
    public Object getUserList() {
      List<Object> users = feignService.getUsers();
      HashMap<Object, Object> map = new HashMap<>();
      map.put("username","consumer1");
      map.put("phone","13000000001");
      users.add(map);
      return users;
    }
}