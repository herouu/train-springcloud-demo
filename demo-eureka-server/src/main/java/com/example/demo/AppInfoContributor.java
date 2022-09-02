package com.example.demo;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class AppInfoContributor implements InfoContributor {


    @Resource
    Environment environment;

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("active", ArrayUtils.isEmpty(environment.getActiveProfiles()) ? environment.getDefaultProfiles() : environment.getActiveProfiles());
        String startTime = environment.getProperty("app.runTime");
        userDetails.put("startTime", startTime);
        try {
            Date date = DateUtils.parseDate(startTime, new String[]{"yyyy-MM-dd HH:mm:ss"});
            String durationTime = DurationFormatUtils.formatDurationWords(new Date().getTime() - date.getTime(), true, true);
            userDetails.put("durationTime", durationTime);
        } catch (ParseException e) {
            //
        }
        builder.withDetails(userDetails);
    }
}
