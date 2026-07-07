package com.app.service;

import com.app.model.FlagResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class RocketflagService {

    private static final Logger log = LoggerFactory.getLogger(RocketflagService.class);

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
            log.info("Consultando RocketFlag — flag={}, env={}", flagId, environment);
            var response = restTemplate.getForObject(url, FlagResponse.class);
            var enabled = response != null && response.isEnabled();
            log.info("RocketFlag retornou enabled={} para flag={}", enabled, flagId);
            return enabled;
        } catch (Exception e) {
            log.warn("RocketFlag indisponivel ou erro — flag={}, env={}, error={}", flagId, environment, e.getMessage());
            return false;
        }
    }
}
