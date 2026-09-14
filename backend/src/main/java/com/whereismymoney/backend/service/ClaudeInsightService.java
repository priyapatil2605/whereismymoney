package com.whereismymoney.backend.service;

import com.whereismymoney.backend.entity.Transaction;
import com.whereismymoney.backend.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@Primary
public class ClaudeInsightService
        implements FinancialInsightService {

    private static final Logger logger = LoggerFactory.getLogger(
            ClaudeInsightService.class);

    private final DeterministicInsightService deterministicInsightService;

    private final TransactionRepository transactionRepository;

    private final ObjectMapper objectMapper;

    private final HttpClient httpClient;

    private final String apiKey;
    private final String apiUrl;
    private final String model;
    private final boolean aiInsightsEnabled;

    public ClaudeInsightService(
            DeterministicInsightService deterministicInsightService,
            TransactionRepository transactionRepository,
            ObjectMapper objectMapper,
            @Value("${claude.api.key:}") String apiKey,
            @Value("${claude.api.url:https://api.anthropic.com/v1/messages}") String apiUrl,
            @Value("${claude.model:claude-sonnet-4-6}") String model,
            @Value("${ai.insights.enabled:false}") boolean aiInsightsEnabled) {

        this.deterministicInsightService = deterministicInsightService;

        this.transactionRepository = transactionRepository;

        this.objectMapper = objectMapper;

        this.apiKey = apiKey;

        this.apiUrl = apiUrl;

        this.model = model;

        this.aiInsightsEnabled = aiInsightsEnabled;

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(
                        Duration.ofSeconds(10))
                .build();
    }

    @Override
    public String answerQuestion(
            Long portfolioId,
            String question) {

        /*
         * AI_INSIGHTS_ENABLED=false:
         *
         * Skip Claude completely and use the
         * deterministic financial analysis.
         */
        if (!aiInsightsEnabled) {

            logger.info(
                    "AI insights disabled. Using deterministic analysis for portfolio {}",
                    portfolioId);

            return deterministicInsightService
                    .answerQuestion(
                            portfolioId,
                            question);
        }

        /*
         * Claude is an optional explanation layer.
         *
         * Any failure falls back to the deterministic
         * financial analysis instead of returning HTTP 500.
         */
        try {

            if (apiKey == null ||
                    apiKey.isBlank()) {

                throw new IllegalStateException(
                        "Claude API key is not configured");
            }

            String financialContext = buildFinancialContext(
                    portfolioId);

            String prompt = """
                    You are the explanation layer for a financial
                    analytics application.

                    IMPORTANT:
                    - Use only the financial data supplied below.
                    - Do not invent numbers.
                    - Do not provide investment advice.
                    - Explain the data clearly and concisely.
                    - If the supplied data does not answer the question,
                      say that clearly.

                    FINANCIAL DATA:
                    %s

                    USER QUESTION:
                    %s
                    """.formatted(
                    financialContext,
                    question);

            Map<String, Object> message = Map.of(
                    "role",
                    "user",
                    "content",
                    prompt);

            Map<String, Object> requestPayload = Map.of(
                    "model",
                    model,
                    "max_tokens",
                    500,
                    "messages",
                    List.of(message));

            String requestBody = objectMapper.writeValueAsString(
                    requestPayload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(
                            URI.create(apiUrl))
                    .timeout(
                            Duration.ofSeconds(30))
                    .header(
                            "Content-Type",
                            "application/json")
                    .header(
                            "x-api-key",
                            apiKey)
                    .header(
                            "anthropic-version",
                            "2023-06-01")
                    .POST(
                            HttpRequest.BodyPublishers
                                    .ofString(
                                            requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers
                            .ofString());

            if (response.statusCode() < 200 ||
                    response.statusCode() >= 300) {

                throw new RuntimeException(
                        "Claude API returned HTTP "
                                + response.statusCode()
                                + ": "
                                + response.body());
            }

            JsonNode root = objectMapper.readTree(
                    response.body());

            JsonNode content = root.path("content");

            if (content.isArray() &&
                    !content.isEmpty()) {

                String answer = content
                        .get(0)
                        .path("text")
                        .asText();

                if (answer != null &&
                        !answer.isBlank()) {

                    return answer;
                }
            }

            throw new RuntimeException(
                    "Claude API returned an empty response");

        } catch (Exception ex) {

            logger.warn(
                    "Claude insight failed for portfolio {}. Falling back to deterministic analysis. Reason: {}",
                    portfolioId,
                    ex.getMessage());

            return deterministicInsightService
                    .answerQuestion(
                            portfolioId,
                            question);
        }
    }

    private String buildFinancialContext(
            Long portfolioId) {

        List<Transaction> transactions = transactionRepository
                .findByPortfolioIdOrderByTransactionDateDesc(
                        portfolioId);

        BigDecimal totalBuy = transactionRepository
                .totalBuyAmount(
                        portfolioId);

        BigDecimal totalSell = transactionRepository
                .totalSellAmount(
                        portfolioId);

        if (totalBuy == null) {
            totalBuy = BigDecimal.ZERO;
        }

        if (totalSell == null) {
            totalSell = BigDecimal.ZERO;
        }

        BigDecimal totalVolume = transactions.stream()
                .map(Transaction::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add);

        return """
                Portfolio ID: %d
                Transaction count: %d
                Total transaction volume: ₹%s
                Total BUY amount: ₹%s
                Total SELL amount: ₹%s
                """.formatted(
                portfolioId,
                transactions.size(),
                totalVolume,
                totalBuy,
                totalSell);
    }
}