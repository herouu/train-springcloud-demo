package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.ff4j.FF4j;
import org.ff4j.web.jersey2.store.FeatureStoreHttp;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;

import java.lang.reflect.Method;

@Slf4j
public class FF4jInterceptor implements MethodInterceptor {

    private FF4j ff4j;

    private LoadBalancerClient loadBalancerClient;
    private ObjectMapper objectMapper = new ObjectMapper();

    public FF4jInterceptor(FF4j ff4j, LoadBalancerClient loadBalancerClient) {
        this.ff4j = ff4j;
        this.loadBalancerClient = loadBalancerClient;
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        ServiceInstance choose = loadBalancerClient.choose("DEMO-EUREKA-PRODUCE-FF4J");
        log.info(objectMapper.writeValueAsString(choose));
        FeatureStoreHttp featureStoreHttp = new FeatureStoreHttp();
        String s = choose.getUri().toString();
        featureStoreHttp.setUrl(s + "/api/ff4j");
        ff4j.setFeatureStore(featureStoreHttp);
        return methodProxy.invoke(ff4j, objects);
    }
}
