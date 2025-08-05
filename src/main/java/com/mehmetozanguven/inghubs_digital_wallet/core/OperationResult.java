package com.mehmetozanguven.inghubs_digital_wallet.core;


import com.mehmetozanguven.inghubs_digital_wallet.core.exception.ApiException;
import com.mehmetozanguven.inghubs_digital_wallet.core.exception.ApiExceptionInfo;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
public class OperationResult<T> {
    private boolean valid;
    private T returnedValue;
    private List<ApiException> exception;
    

    private OperationResult(boolean valid, @Nullable T returnedValue, List<ApiException> exception) {
        this.valid = valid;
        this.returnedValue = returnedValue;
        this.exception = exception;
    }

    public void validateResult() throws ApiException {
        if (!isValid()) {
            throw this.getException().getFirst();
        }
    }

    public static<T> OperationResultBuilder<T> builder() {
        return new OperationResultBuilder<>();
    }

    public static class OperationResultBuilder<T> {
        private boolean valid;
        private T returnedValue;
        private List<ApiException> exception;

        public OperationResultBuilder() {
            valid = true;
            exception = new ArrayList<>();
            
        }

        public OperationResultBuilder<T> addListOfExceptions(List<ApiException> apiExceptions) {
            this.exception.addAll(apiExceptions);
            this.valid = false;
            return this;
        }

        public OperationResultBuilder<T> addException(ApiException apiException) {
            this.exception.add(apiException);
            this.valid = false;
            return this;
        }

        public OperationResultBuilder<T> addException(ApiExceptionInfo apiExceptionInfo) {
            ApiException apiException = new ApiException(apiExceptionInfo);
            this.exception.add(apiException);
            this.valid = false;
            return this;
        }

        public OperationResultBuilder<T> addException(ApiExceptionInfo apiExceptionInfo, String message) {
            ApiExceptionInfo customized = new ApiExceptionInfo() {
                @Override
                public String getCode() {
                    return apiExceptionInfo.getCode();
                }

                @Override
                public String getMessage() {
                    return message;
                }

                @Override
                public HttpStatus getExceptionStatus() {
                    return apiExceptionInfo.getExceptionStatus();
                }
            };
            ApiException apiException = new ApiException(customized);
            this.exception.add(apiException);
            this.valid = false;
            return this;
        }

        public OperationResultBuilder<T> valid(boolean valid) {
            this.valid = valid;
            return this;
        }

        public OperationResultBuilder<T> addReturnedValue(T body) {
            this.returnedValue = body;
            this.valid = true;
            return this;
        }

        public OperationResult<T> build() {
            if (!this.valid && this.exception.isEmpty()) {
                log.error("There must at least one exception if operationResult is not valid");
                throw new RuntimeException("Invalid operationResult status");
            }
            return new OperationResult<>(this.valid, this.returnedValue, this.exception);
        }
    }
}
