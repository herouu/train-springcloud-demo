package com.example.demoeurakeconsumerfinal;

import com.netflix.discovery.converters.Auto;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author alertcode
 * @date 2018-08-26
 * @copyright alertcode.top
 */
@RestController
public class FinalConsumerController {

  @Autowired
  private FinalService finalService;

  @GetMapping("/final")
  public Object getFinalConsumer(){
    List<Object> o = finalService.finalConsumer();
    o.add("over!");
    return o;
  }



}
