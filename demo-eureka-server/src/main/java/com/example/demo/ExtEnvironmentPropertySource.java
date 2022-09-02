package com.example.demo;

import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Properties;

@Component
public class ExtEnvironmentPropertySource implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Properties properties = new Properties();
        properties.setProperty("app.runTime", DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        environment.getPropertySources().addLast(new PropertiesPropertySource("app", properties));
    }
}