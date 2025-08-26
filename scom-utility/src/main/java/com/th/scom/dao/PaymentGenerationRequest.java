package com.th.scom.dao;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

/**
 * PaymentGenerationRequest contains the details required for generating a payment redirect URL.
 * 
 * Fields:
 * - pricePoint: The price point of the service.
 * - service: The name of the service.
 * - urlOk: The URL to redirect to upon successful payment.
 * - urlNok: The URL to redirect to if payment is not successful.
 * - urlError: The URL to redirect to in case of an error.
 * - transactionId: The transaction ID for the payment.
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PaymentGenerationRequest {
    private String msisdn;
    private String offerCode;
    private String redirectUrl;
    private String requestid;
    private String sourceIp;
    private String userAgent;
}
