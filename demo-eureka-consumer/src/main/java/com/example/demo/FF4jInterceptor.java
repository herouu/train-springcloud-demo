package com.example.demo;

import cn.hutool.json.JSONUtil;
import com.netflix.loadbalancer.Server;
import lombok.extern.slf4j.Slf4j;
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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class FF4jInterceptor implements MethodInterceptor {


    public static Boolean chooseNull = false;
    @Autowired
    FF4jConfigProperties ff4jConfigProperties;
    @Autowired
    LoadBalancerClient loadBalancerClient;
    @Autowired
    DiscoveryClient discoveryClient;
    private String SERVICE_ID = "demo-eureka-produce-ff4j";
    private FF4j ff4j;
    private AtomicInteger nextServerCyclicCounter;
    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private volatile List<String> aliveServer = new ArrayList<>();
    private volatile ScheduledFuture<?> scheduledFuture;


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


    private String ipAddress() {
        ServiceInstance choose = loadBalancerClient.choose(SERVICE_ID);
        if (chooseNull) {
            choose = null;
        }
        if (Objects.isNull(choose)) {
//            List<ServiceInstance> instances = discoveryClient.getInstances(SERVICE_ID);
//            List<String> discoveryList = instances.stream().map(item -> item.getUri().toString()).collect(Collectors.toList());
//            if (CollectionUtils.isNotEmpty(discoveryList)) {
//                return chooseIpAddress(discoveryList);
//            }
            return chooseIpAddress(ff4jConfigProperties.getIpAddress());
        } else {
            // clear service scheduled executor
            clear();
        }
        log.info(JSONUtil.toJsonStr(choose));
        return choose.getUri().toString();

    }


    public String chooseIpAddress(List<String> servers) {
        if (Objects.isNull(scheduledFuture)) {
            synchronized (FF4jInterceptor.class) {
                CountDownLatch countDownLatch = new CountDownLatch(1);
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
                scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(runnable, 0, 10, TimeUnit.SECONDS);
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }
        int serverCount = aliveServer.size();
        if (serverCount <= 0) {
            throw new RuntimeException("no server can invoke");
        }
        int nextServerIndex = incrementAndGetModulo(serverCount);
        return servers.get(nextServerIndex);
    }

    private void clear() {
        if (Objects.nonNull(scheduledFuture)) {
            synchronized (FF4jInterceptor.class) {
                aliveServer.clear();
                scheduledFuture.cancel(true);
                scheduledFuture = null;
            }
        }
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
