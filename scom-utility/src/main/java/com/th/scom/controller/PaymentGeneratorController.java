package com.th.scom.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.th.scom.dao.PaymentGenerationRequest;
import com.th.scom.service.PaymentGenerationService;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * PaymentGeneratorController handles API requests for generating payment redirect URLs.
 * 
 * Endpoint:
 * POST /api/v1/payment/redirect
 * 
 * Request Body:
 * {@link PaymentGenerationRequest} - contains details like pricePoint, service, urlOk, urlNok, urlError, and transactionId.
 * 
 * Response:
 * A map containing the reference and redirect URL.
 */
@RestController
@CrossOrigin(origins = {"https://saf.wellnesss360.com", "http://localhost:3000"})
@RequestMapping("/api/v1/payment")
@Tag(name = "Payment API", description = "API for generating payment redirect URLs")
public class PaymentGeneratorController {
    private static final Logger logger = LoggerFactory.getLogger(PaymentGeneratorController.class);

    @Autowired
    private PaymentGenerationService paymentGenerationService;

    @PostMapping("/redirect")
    @Operation(summary = "Generate payment redirect URL", description = "Generates a payment redirect URL using the provided payment details.")
    public Map<String, String> getRedirectUrl(@RequestBody PaymentGenerationRequest paymentGenerationRequest) {
    	logger.info("Received request to generate payment redirect URL with details: {}", paymentGenerationRequest);
        return paymentGenerationService.getRedirectUrl(paymentGenerationRequest);
    }
    
    @Hidden
    @RequestMapping(value = "/test", method = { RequestMethod.POST,  RequestMethod.GET })
    @Operation(summary = "Test URL", description = "Just for Test")
    public Map<String, String> testUrl() {
    	logger.info("Got request on Test URL");
        return Map.of("reference","test1","url","test2");
    }
}