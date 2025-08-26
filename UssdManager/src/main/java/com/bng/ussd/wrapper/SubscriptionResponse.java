package com.bng.ussd.wrapper;


	public class SubscriptionResponse {
		private String status;
		private String reason;
		private String serviceId;
		private String serviceName;
		private String price;
		private String responseCode;
		private String subServiceName;
		private String subServiceId;
		private String subType;
		private String subTimeLeft;
		private String msisdn;
		private String transactionId;
		private String subStartDate;
		private String subEndDate;
		private String currentStatus;
		private Integer callAttempts;		
		private String language;	
		private String giftId;
		private Integer successCallCount;
		
		 /*private Date lastRenewDate;
	     private Date lastCallDate;
	     private String transactionStatus;
	     private String mdnType;
	     private String circle;
	     private String country;
	     private boolean isFirstCaller ;
	     private String primaryActMode;
	     private String secondaryActMode;
	     private String errorMsg;
		*/
		public String getCurrentStatus() {
			return currentStatus;
		}
		public void setCurrentStatus(String currentStatus) {
			this.currentStatus = currentStatus;
		}
		
		public String getSubStartDate() {
			return subStartDate;
		}
		public void setSubStartDate(String subStartDate) {
			this.subStartDate = subStartDate;
		}
		
		public String getSubEndDate() {
			return subEndDate;
		}
		public void setSubEndDate(String subEndDate) {
			this.subEndDate = subEndDate;
		}
		public String getTransactionId() {
			return transactionId;
		}
		public void setTransactionId(String transactionId) {
			this.transactionId = transactionId;
		}
		public String getSubServiceId() {
			return subServiceId;
		}
		public void setSubServiceId(String subServiceId) {
			this.subServiceId = subServiceId;
		}
		public String getMsisdn() {
			return msisdn;
		}
		public void setMsisdn(String msisdn) {
			this.msisdn = msisdn;
		}
		public String getSubType() {
			return subType;
		}
		public void setSubType(String subType) {
			this.subType = subType;
		}
		
		public String getSubTimeLeft() {
			return subTimeLeft;
		}
		public void setSubTimeLeft(String subTimeLeft) {
			this.subTimeLeft = subTimeLeft;
		}
		
		
		public String getStatus() {
			return status;
		}
		public String getReason() {
			return reason;
		}
		public String getSubServiceName() {
			return subServiceName;
		}
		public void setStatus(String status) {
			this.status = status;
		}
		public void setReason(String reason) {
			this.reason = reason;
		}
		public void setSubServiceName(String subServiceName) {
			this.subServiceName = subServiceName;
		}
		
		public String getServiceId() {
			return serviceId;
		}
		public void setServiceId(String serviceId) {
			this.serviceId = serviceId;
		}
		public String getServiceName() {
			return serviceName;
		}
		public void setServiceName(String serviceName) {
			this.serviceName = serviceName;
		}
		public String getPrice() {
			return price;
		}
		public void setPrice(String price) {
			this.price = price;
		}
		
		public String getResponseCode() {
			return responseCode;
		}
		public void setResponseCode(String responseCode) {
			this.responseCode = responseCode;
		}
		public Integer getCallAttempts() {
			return callAttempts;
		}
		public void setCallAttempts(Integer callAttempts) {
			this.callAttempts = callAttempts;
		}
		public String getLanguage() {
			return language;
		}
		public void setLanguage(String language) {
			this.language = language;
		}
		public String getGiftId() {
			return giftId;
		}
		public void setGiftId(String giftId) {
			this.giftId = giftId;
		}
		public Integer getSuccessCallCount() {
			return successCallCount;
		}
		public void setSuccessCallCount(Integer successCallCount) {
			this.successCallCount = successCallCount;
		}
		
		
	}

