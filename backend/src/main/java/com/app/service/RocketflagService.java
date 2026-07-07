package com.app.service;

import com.app.model.FlagResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class RocketflagService {

    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final String flagId;
    private final String environment;

    public RocketflagService(
            RestTemplate restTemplate,
            @Value("${rocketflag.api-url}") String apiUrl,
            @Value("${rocketflag.flag-id}") String flagId,
            @Value("${rocketflag.environment}") String environment) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
        this.flagId = flagId;
        this.environment = environment;
    }

    public boolean isFeatureEnabled() {
        try {
            var env = URLEncoder.encode(environment, StandardCharsets.UTF_8);
            var url = apiUrl + "/v1/flags/" + flagId + "?env=" + env;
            var response = restTemplate.getForObject(url, FlagResponse.class);
            return response != null && response.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }
}
