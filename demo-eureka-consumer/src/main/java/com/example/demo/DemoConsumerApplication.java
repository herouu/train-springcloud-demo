package com.example.demo;

import cn.hutool.core.util.ZipUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import feign.Logger;
import feign.Response;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.ff4j.FF4j;
import org.ff4j.utils.IOUtil;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@FeignClient(value = "eureka-produce-demo")
interface FeignService {

    @RequestMapping(value = "/produce")
    Response getUsers();

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

    JsonFactory jsonFactory = new JsonFactory();

    ObjectMapper objectMapper = new ObjectMapper();

    public static byte[] uncompress(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        try {
            GZIPInputStream ungzip = new GZIPInputStream(in);
            byte[] buffer = new byte[256];
            int n;
            while ((n = ungzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
        } catch (IOException e) {

        }

        return out.toByteArray();
    }

    @GetMapping("/consumer")
    public Object getUserList() throws IOException {
        long l = System.currentTimeMillis();
        Response user = feignService.getUsers();
        InputStream inputStream = user.body().asInputStream();
        System.out.println(System.currentTimeMillis() - l);
        GZIPInputStream gzipOutputStream = new GZIPInputStream(inputStream, 5120);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(5120);
        IOUtils.copy(gzipOutputStream, outputStream);
        byte[] bytes = outputStream.toByteArray();
        List<?> o = objectMapper.readValue(bytes, new TypeReference<List>() {
        });
        System.out.println(System.currentTimeMillis() - l);
        return o;
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
