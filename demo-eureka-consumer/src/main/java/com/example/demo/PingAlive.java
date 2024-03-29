package com.example.demo;

import com.netflix.loadbalancer.IPing;
import com.netflix.loadbalancer.Server;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class PingAlive implements IPing {

    private static final Logger LOGGER = LoggerFactory.getLogger(PingAlive.class);
    String pingAppendString = "";
    boolean isSecure = false;
    String expectedContent = null;

    public PingAlive() {
    }

    public PingAlive(boolean isSecure, String pingAppendString) {
        this.isSecure = isSecure;
        this.pingAppendString = pingAppendString != null ? pingAppendString : "";
    }

    public String getPingAppendString() {
        return this.pingAppendString;
    }

    public void setPingAppendString(String pingAppendString) {
        this.pingAppendString = pingAppendString != null ? pingAppendString : "";
    }

    public boolean isSecure() {
        return this.isSecure;
    }

    public void setSecure(boolean isSecure) {
        this.isSecure = isSecure;
    }

    public String getExpectedContent() {
        return this.expectedContent;
    }

    public void setExpectedContent(String expectedContent) {
        this.expectedContent = expectedContent;
    }

    @Override
    public boolean isAlive(Server server) {
        String urlStr = "";
        if (this.isSecure) {
            urlStr = "https://";
        } else {
            urlStr = "http://";
        }

        urlStr = urlStr + server.getId();
        urlStr = urlStr + this.getPingAppendString();
        boolean isAlive = false;
        HttpClient httpClient = new DefaultHttpClient();
        HttpUriRequest getRequest = new HttpGet(urlStr);
        String content = null;

        try {
            HttpResponse response = httpClient.execute(getRequest);
            content = EntityUtils.toString(response.getEntity());
            isAlive = response.getStatusLine().getStatusCode() == 200;
            if (this.getExpectedContent() != null) {
                LOGGER.debug("content:" + content);
                if (content == null) {
                    isAlive = false;
                } else if (content.equals(this.getExpectedContent())) {
                    isAlive = true;
                } else {
                    isAlive = false;
                }
            }
        } catch (IOException var11) {
            //ignore
        } finally {
            getRequest.abort();
        }

        return isAlive;
    }
}
