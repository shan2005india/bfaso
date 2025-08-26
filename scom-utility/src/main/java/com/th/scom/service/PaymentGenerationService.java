package com.th.scom.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.th.scom.dao.PaymentGenerationRequest;
import com.th.scom.model.PaymentGenerationResponse;
import com.th.scom.model.TokenResponse;

/**
 * PaymentGenerationService handles the business logic for generating tokens and payment redirect URLs.
 * 
 * It communicates with external APIs to generate tokens and uses these tokens to fetch payment URLs.
 */
@Service
@CacheConfig(cacheNames = "tokens")
public class PaymentGenerationService {
	private static final Logger logger = LoggerFactory.getLogger(PaymentGenerationService.class);
	
    @Value("${payment.client.token.url}")
    private String tokenUrl;

    @Value("${payment.token.expiry.duration.minutes:30}")
    private long tokenExpiryDurationMinutes;

    private long tokenExpiryTime;
    
    private TokenResponse tokenResponse;

    @Value("${payment.client.id}")
    private String clientId;

    @Value("${payment.client.secret}")
    private String clientSecret;
    
    @Value("${payment.request.url}")
    private String requestUrl;

    @Value("${payment.redirect_url}")
    private String redirectUrl;

    @Value("${payment.offercode}")
    private String offercode;

//    @Value("${payment.requestid}")
//    private String requestid;

    @Value("${payment.source_ip}")
    private String source_ip;
    
    @Value("${url.connectTimeout}")
    private Integer urlConnectTimeout;
    
    @Value("${url.readTimeout}")
    private Integer urlReadTimeout;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Retrieves the token from cache or generates a new one if expired.
     * 
     * @return The access token.
     */
    @Cacheable
    public String getToken() {
    	logger.info("Fetching token from cache or generating a new one if expired");
        if (isTokenExpired()) {
            return updateToken();
        }
        return tokenExpiryTime > 0 ? this.tokenResponse.getToken() : generateToken();
    }

    /**
     * Updates and returns a new token.
     * 
     * @return The new access token.
     */
    @CachePut
    public String updateToken() {
    	logger.info("Updating and generating a new token");
        return generateToken();
    }

    /**
     * Generates a new token and updates the expiry time.
     * 
     * @return The generated access token.
     */
    private String generateToken() {
    	logger.info("Generating a new token");
        tokenExpiryTime = System.currentTimeMillis() + (tokenExpiryDurationMinutes * 60 * 1000);
        var requestBody = Map.of(
                "username", clientId,
                "password", clientSecret
        );
        
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(urlConnectTimeout); // Set connection timeout (in milliseconds)
        requestFactory.setReadTimeout(urlReadTimeout); // Set socket timeout (in milliseconds)
        restTemplate.setRequestFactory(requestFactory);

        HttpEntity<Map> entity = new HttpEntity<Map>(requestBody, httpHeaders);
        
        String response = restTemplate.postForObject(tokenUrl, entity, String.class);
        return extractAccessToken(response);
    }
    
    private String generateToken_old() {
    	logger.info("Generating a new token");
        tokenExpiryTime = System.currentTimeMillis() + (tokenExpiryDurationMinutes * 60 * 1000);
        var requestBody = Map.of(
                "grant_type", "client_credentials",
                "client_id", clientId,
                "client_secret", clientSecret
        );
        
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(urlConnectTimeout); // Set connection timeout (in milliseconds)
        requestFactory.setReadTimeout(urlReadTimeout); // Set socket timeout (in milliseconds)
        restTemplate.setRequestFactory(requestFactory);

        String response = restTemplate.postForObject(tokenUrl, requestBody, String.class);
        return extractAccessToken(response);
    }

    /**
     * Checks if the current token is expired or about to expire.
     * 
     * @return True if the token is expired or expiring soon, false otherwise.
     */
    private boolean isTokenExpired() {
    	logger.info("Checking if the token is expired or about to expire");
        return System.currentTimeMillis() > (tokenExpiryTime - 60000);
    }

    /**
     * Extracts the access token from the response.
     * 
     * @param response The response containing the token.
     * @return The extracted access token.
     */
    private String extractAccessToken(String response) {
    	logger.info("Extracting access token from response");
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TokenResponse tokenResponse = objectMapper.readValue(response, TokenResponse.class);
            this.tokenResponse = tokenResponse;
            return tokenResponse.getToken();
        } catch (JsonProcessingException e) {
            logger.error("Failed to extract access token from response", e);
            throw new RuntimeException("Failed to extract access token", e);
        }
    }

    /**
     * Generates a payment redirect URL using the provided payment details.
     * 
     * @param paymentRequest The payment details.
     * @return A map containing the reference and redirect URL.
     */
    public Map<String, String> getRedirectUrl(PaymentGenerationRequest paymentRequest) {
    	logger.info("Generating redirect URL for payment request: {}", paymentRequest);
    	
//    	if()
    	
        var token = getToken();
        // If the token is expired or invalid, update it
        if (token == null) {
        	logger.info("Token is null, generating");
            token = updateToken();
        }

        logger.info("Generated Token: "+token);
        // Set default values if not provided in the request
        paymentRequest.setOfferCode(paymentRequest.getOfferCode() == null ? offercode : paymentRequest.getOfferCode());
        paymentRequest.setSourceIp(paymentRequest.getSourceIp() == null ? source_ip : paymentRequest.getSourceIp());
        paymentRequest.setRedirectUrl(paymentRequest.getRedirectUrl() == null ? redirectUrl : paymentRequest.getRedirectUrl());
        paymentRequest.setRequestid(paymentRequest.getRequestid() == null ? UUID.randomUUID().toString() : paymentRequest.getRequestid());

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set("Authorization", "Bearer " + token);
        
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(urlConnectTimeout); // Set connection timeout (in milliseconds)
        requestFactory.setReadTimeout(urlReadTimeout); // Set socket timeout (in milliseconds)
        restTemplate.setRequestFactory(requestFactory);

        logger.info("Sending request: "+paymentRequest+" header: "+httpHeaders);
        
        HttpEntity<PaymentGenerationRequest> entity = new HttpEntity<>(paymentRequest, httpHeaders);
        String response = restTemplate.postForObject(requestUrl, entity, String.class);
        
        logger.info("Got response: "+response);
        return extractRedirectData(response);
    }

    /**
     * Extracts the reference and URL from the response.
     * 
     * @param response The response containing the reference and URL.
     * @return A map containing the reference and URL.
     */
    private Map<String, String> extractRedirectData(String response) {
    	logger.info("Extracting redirect data from response");
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            PaymentGenerationResponse pgresp = objectMapper.readValue(response, PaymentGenerationResponse.class);
            String reference = pgresp.getMfId();
            String url = pgresp.getCgUrl();
            return Map.of("reference", reference, "url", url, "msisdn", pgresp.getMsisdn());
        } catch (JsonProcessingException e) {
            logger.error("Failed to extract redirect data from response", e);
            throw new RuntimeException("Failed to extract redirect data", e);
        }
    }
}