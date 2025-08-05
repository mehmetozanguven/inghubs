package com.mehmetozanguven.inghubs_digital_wallet.core.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
    private final ApiExceptionInfo apiExceptionInfo;

    public ApiException(ApiExceptionInfo apiExceptionInfo) {
        super(apiExceptionInfo.getMessage());
        this.apiExceptionInfo = apiExceptionInfo;
    }

    public ApiExceptionInfo getStarterExceptionInformation() {
        return apiExceptionInfo;
    }

    public String getExceptionMessage() {
        return apiExceptionInfo.getMessage();
    }

    public String getCode() {
        return apiExceptionInfo.getCode();
    }

    public HttpStatus getExceptionStatus() {
        return apiExceptionInfo.getExceptionStatus();
    }

    public String getOnlyErrorCodeAndMessage() {
        return "ApiException{" +
                "code=" + apiExceptionInfo.getCode() +
                ", message='" + apiExceptionInfo.getMessage() +
                '}';
    }
}
