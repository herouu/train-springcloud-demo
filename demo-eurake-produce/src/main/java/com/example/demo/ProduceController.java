package com.example.demo;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProduceController {

  @GetMapping("/produce")
  public List<User> userList() {
    return createUsers();
  }

  @Data
  class User {

    private String username;
    private String phone;
  }

  private List<User> createUsers() {
    ArrayList<User> o = new ArrayList<>();
    for (int i = 0; i <= 5; i++) {
      User user = new User();
      user.setPhone("1388948577" + i);
      user.setUsername("zhangsan" + i);
      o.add(user);
    }
    return o;
  }


}