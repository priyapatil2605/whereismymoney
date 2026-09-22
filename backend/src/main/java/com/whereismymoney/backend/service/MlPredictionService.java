package com.whereismymoney.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MlPredictionService {

    private final String mlServiceUrl;
    private final RestTemplate restTemplate;

    public MlPredictionService(
            @Value("${ml.service.url}") String mlServiceUrl) {

        this.mlServiceUrl = mlServiceUrl;
        this.restTemplate = new RestTemplate();
    }

    public String predict(
            String symbol,
            String period,
            int horizon) {

        String jsonBody = """
                {
                    "symbol": "%s",
                    "period": "%s",
                    "horizon": %d
                }
                """.formatted(
                symbol,
                period,
                horizon);

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(
                jsonBody,
                headers);

        ResponseEntity<String> response = restTemplate.exchange(
                mlServiceUrl + "/predict",
                HttpMethod.POST,
                request,
                String.class);

        return response.getBody();
    }

    public String getMarketData(
            String symbol) {

        return restTemplate.getForObject(
                mlServiceUrl
                        + "/market/"
                        + symbol,
                String.class);
    }

    public String getFeatures(
            String symbol) {

        return restTemplate.getForObject(
                mlServiceUrl
                        + "/features/"
                        + symbol,
                String.class);
    }

    public String getWalkForwardValidation(
            String symbol) {

        return restTemplate.getForObject(
                mlServiceUrl
                        + "/walk-forward/"
                        + symbol,
                String.class);
    }

    public String advancedBacktest(
            String symbol,
            String period,
            double initialCapital,
            double transactionCostBps,
            int fastWindow,
            int slowWindow) {

        String jsonBody = """
                {
                    "symbol": "%s",
                    "period": "%s",
                    "initial_capital": %s,
                    "transaction_cost_bps": %s,
                    "fast_window": %d,
                    "slow_window": %d
                }
                """.formatted(
                symbol,
                period,
                initialCapital,
                transactionCostBps,
                fastWindow,
                slowWindow);

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(
                jsonBody,
                headers);

        ResponseEntity<String> response = restTemplate.exchange(
                mlServiceUrl
                        + "/advanced-backtest",
                HttpMethod.POST,
                request,
                String.class);

        return response.getBody();
    }
}