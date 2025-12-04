package org.banking.crypto.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoinGeckoService {

    private final WebClient webClient;

    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final int MAX_RETRIES = 3;

    public Map<String, BigDecimal> getPrices(List<String> cryptoIds, String currency) {
        validateInput(cryptoIds, currency);

        String ids = String.join(",", cryptoIds);

        try {
            Map<String, Map<String, BigDecimal>> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/simple/price")
                            .queryParam("ids", ids)
                            .queryParam("vs_currencies", currency)
                            .queryParam("include_24hr_change", "true") // Added useful data
                            .queryParam("include_last_updated_at", "true")
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                        log.error("Client error fetching prices: {}", clientResponse.statusCode());
                        throw new RuntimeException("Client error: " + clientResponse.statusCode());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> {
                        log.error("Server error fetching prices: {}", clientResponse.statusCode());
                        throw new RuntimeException(
                                "Server error: " + clientResponse.statusCode());
                    })
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Map<String, BigDecimal>>>() {})
                    .timeout(TIMEOUT)
                    .retry(MAX_RETRIES)
                    .block();

            return extractPrices(response, currency);

        } catch (Exception e) {
            log.error("Error fetching crypto prices for {} in {}: {}", cryptoIds, currency, e.getMessage());
            throw new RuntimeException("Failed to fetch crypto prices: " + e.getMessage(), e);
        }
    }

    private void validateInput(List<String> cryptoIds, String currency) {
        if (cryptoIds == null || cryptoIds.isEmpty()) {
            throw new IllegalArgumentException("cryptoIds cannot be null or empty");
        }
        if (currency == null || currency.trim().isEmpty()) {
            throw new IllegalArgumentException("currency cannot be null or empty");
        }
        if (cryptoIds.size() > 50) { // CoinGecko has limits
            throw new IllegalArgumentException("Maximum 50 crypto IDs allowed per request");
        }
    }

    private Map<String, BigDecimal> extractPrices(Map<String, Map<String, BigDecimal>> response, String currency) {
        if (response == null || response.isEmpty()) {
            throw new RuntimeException("No data returned from API");
        }

        Map<String, BigDecimal> prices = new HashMap<>();
        for (Map.Entry<String, Map<String, BigDecimal>> entry : response.entrySet()) {
            String coin = entry.getKey();
            Map<String, BigDecimal> currencyMap = entry.getValue();

            if (currencyMap == null || !currencyMap.containsKey(currency)) {
                log.warn("No price data found for {} in currency {}", coin, currency);
                continue; // Skip instead of throwing exception
            }

            BigDecimal price = currencyMap.get(currency);
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("Invalid price for {}: {}", coin, price);
                continue;
            }

            prices.put(coin, price);
        }

        if (prices.isEmpty()) {
            throw new RuntimeException("No valid price data found for the requested cryptocurrencies");
        }

        log.info("Successfully fetched prices for {} cryptocurrencies", prices.size());
        return prices;
    }


    public BigDecimal getSinglePrice(String cryptoId, String currency) {
        Map<String, BigDecimal> prices = getPrices(List.of(cryptoId), currency);
        return prices.get(cryptoId);
    }

    public Map<String, Map<String, Object>> getPriceWithChange(List<String> cryptoIds, String currency) {
        String ids = String.join(",", cryptoIds);

        Map<String, Map<String, Object>> response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/simple/price")
                        .queryParam("ids", ids)
                        .queryParam("vs_currencies", currency)
                        .queryParam("include_24hr_change", "true")
                        .queryParam("include_market_cap", "true")
                        .queryParam("include_24hr_vol", "true")
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Map<String, Object>>>() {})
                .timeout(TIMEOUT)
                .block();

        return response != null ? response : Map.of();
    }

    public Map<String, Object> getCoinDetail(String coinId) {
        return webClient.get()
                .uri("/coins/{id}?localization=false&tickers=false&market_data=true&community_data=false&developer_data=false&sparkline=false",
                        coinId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .timeout(TIMEOUT)
                .block();
    }
}
