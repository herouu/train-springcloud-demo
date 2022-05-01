package com.example.demo;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.loadbalancer.Server;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.ff4j.FF4j;
import org.ff4j.web.jersey2.store.FeatureStoreHttp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Component
public class FF4jInterceptor implements MethodInterceptor {


    @Autowired
    FF4jConfigProperties ff4jConfigProperties;

    @Autowired
    LoadBalancerClient loadBalancerClient;

    @Autowired
    DiscoveryClient discoveryClient;

    private String SERVICE_ID = "demo-eureka-produce-ff4j";
    private FF4j ff4j;
    private ObjectMapper objectMapper = new ObjectMapper();
    private AtomicInteger nextServerCyclicCounter;
    private ScheduledExecutorService scheduledExecutorService;
    private volatile List<String> aliveServer = new ArrayList<>();
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    public FF4jInterceptor() {
        this.ff4j = new FF4j();
        nextServerCyclicCounter = new AtomicInteger(0);
    }

    @PostConstruct
    public void init() {

    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        FeatureStoreHttp featureStoreHttp = new FeatureStoreHttp();
        String requestUrl = ipAddress() + "/api/ff4j";
        log.info("requestUrl:{}", requestUrl);
        featureStoreHttp.setUrl(requestUrl);
        ff4j.setFeatureStore(featureStoreHttp);
        return methodProxy.invoke(ff4j, objects);
    }


    private String ipAddress() throws JsonProcessingException {
        ServiceInstance choose = loadBalancerClient.choose(SERVICE_ID);
        if (Objects.isNull(choose)) {
            List<ServiceInstance> instances = discoveryClient.getInstances(SERVICE_ID);
            List<String> discoveryList = instances.stream().map(item -> item.getUri().toString()).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(discoveryList)) {
                return chooseIpAddress(discoveryList);
            }
            return chooseIpAddress(ff4jConfigProperties.getIpAddress());
        }
        log.info(objectMapper.writeValueAsString(choose));
        return choose.getUri().toString();

    }


    public String chooseIpAddress(List<String> servers) {
        if (Objects.isNull(scheduledExecutorService)) {
            synchronized (FF4jInterceptor.class) {
                scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
                Runnable runnable = () -> {
                    List<String> filterResult = new ArrayList<>();
                    // filter died server
                    for (String server : servers) {
                        PingAlive p = new PingAlive(false, "/api/ff4j");
                        Server s = new Server(server);
                        if (p.isAlive(s)) {
                            filterResult.add(server);
                        }
                    }
                    aliveServer = filterResult;
                    log.info(JSONUtil.toJsonStr(aliveServer));
                    countDownLatch.countDown();
                };
                scheduledExecutorService.scheduleWithFixedDelay(runnable, 0, 30, TimeUnit.SECONDS);
            }
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            //ignore
        }
        int serverCount = aliveServer.size();
        if (serverCount <= 0) {
            throw new RuntimeException("no server can invoke");
        }
        int nextServerIndex = incrementAndGetModulo(serverCount);
        return servers.get(nextServerIndex);
    }

    /**
     * Inspired by the implementation of {@link AtomicInteger#incrementAndGet()}.
     *
     * @param modulo The modulo to bound the value of the counter.
     * @return The next value.
     */
    private int incrementAndGetModulo(int modulo) {
        for (; ; ) {
            int current = nextServerCyclicCounter.get();
            int next = (current + 1) % modulo;
            if (nextServerCyclicCounter.compareAndSet(current, next))
                return next;
        }
    }

}
