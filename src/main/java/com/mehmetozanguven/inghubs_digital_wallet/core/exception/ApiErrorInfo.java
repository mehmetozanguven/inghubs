package com.mehmetozanguven.inghubs_digital_wallet.core.exception;

import org.springframework.http.HttpStatus;

import java.util.Objects;

public enum ApiErrorInfo implements ApiExceptionInfo {
    INVALID_REQUEST(
            "-2",
            "Request is invalid",
            HttpStatus.OK
    ),
    SOMETHING_WENT_WRONG(
            "-1",
            "Something went wrong",
            HttpStatus.OK
    ),
    CONSTRAINT_VIOLATION_ERROR(
            "1",
            "Something went wrong",
            HttpStatus.BAD_REQUEST
    ),
    METHOD_ARGUMENT_NOT_VALID_ERROR(
            "3",
            "Something went wrong",
            HttpStatus.BAD_REQUEST
    ),
    CUSTOMER_ALREADY_EXISTS(
            "C-1",
            "Customer already exists",
            HttpStatus.OK
    ),
    TRANSACTION_OPERATION_FAILED("T-1", "Transaction operation failed", HttpStatus.OK),
    TRANSACTION_NOT_FOUND("T-2", "Transaction operation failed", HttpStatus.OK),
    AUTHENTICATION_EXCEPTION(
            "AUTH-1",
            "Authentication exception",
            HttpStatus.UNAUTHORIZED
    ),
    BAD_CREDENTIALS_EXCEPTION(
            "AUTH-2",
            "Authentication exception",
            HttpStatus.UNAUTHORIZED
    ),
    ACCOUNT_DISABLED_EXCEPTION(
            "AUTH-3",
            "Authentication exception",
            HttpStatus.UNAUTHORIZED
    ),
    ACCESS_DENIED_EXCEPTION("AUTH-4", "Access denied", HttpStatus.BAD_REQUEST),
    WALLET_INVALID_REQUEST(
            "W-1",
            "Wallet request is invalid",
            HttpStatus.OK
    ),
    WALLET_NOT_OPEN_FOR_WITHDRAW(
            "W-2",
            "Wallet not open for withdraw",
            HttpStatus.OK
    )
    ;

    public final String code;
    public final String message;
    public final HttpStatus httpStatus;

    ApiErrorInfo(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public HttpStatus getExceptionStatus() {
        if (Objects.nonNull(httpStatus)) {
            return httpStatus;
        }
        return ApiExceptionInfo.super.getExceptionStatus();
    }


    public String toString() {
        return "ApiErrorInformation{code=" + this.code + ", message='" + this.message + "'}";
    }
}
