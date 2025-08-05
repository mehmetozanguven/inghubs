package com.mehmetozanguven.inghubs_digital_wallet.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mehmetozanguven.inghubs_digital_wallet.core.exception.ApiProblemDetail;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ToString
@Slf4j
public class GenericApiResponse<T> {
    public static final String MESSAGE_KEY = "message";
    public static final String ERROR_CODE_KEY = "code";
    public T response;
    public boolean isSuccess;
    public List<ApiProblemDetail> errorResponses;
    public @JsonIgnore HttpStatus httpStatus;
    public int httpStatusCode;

    public GenericApiResponse() {

    }

    public GenericApiResponse(T response, boolean isSuccess, List<ApiProblemDetail> errorResponses, HttpStatus httpStatus, int httpStatusCode) {
        this.response = response;
        this.isSuccess = isSuccess;
        this.errorResponses = errorResponses;
        this.httpStatus = httpStatus;
        this.httpStatusCode = httpStatusCode;
    }

    public static class Builder<T> {
        private T response;
        private boolean isSuccess;
        private HttpStatus httpStatusCode;
        private List<ApiProblemDetail> errorResponses;


        public Builder() {
            this.errorResponses = new ArrayList<>();

        }

        public Builder<T> httpStatusCode(HttpStatus httpStatus) {
            httpStatusCode = httpStatus;
            return this;
        }

        public Builder<T> isSuccess(boolean isSuccess) {
            this.isSuccess = isSuccess;
            if (isSuccess) {
                return httpStatusCode(HttpStatus.OK);
            } else {
                return httpStatusCode(HttpStatus.BAD_REQUEST);
            }
        }

        public Builder<T> addErrorResponse(ApiProblemDetail errorResponse) {
            this.errorResponses.add(errorResponse);
            return this;
        }

        public Builder<T> addErrors(List<ApiProblemDetail> errorResponses) {
            this.errorResponses.addAll(errorResponses);
            return this;
        }

        public Builder<T> response(T response) {
            this.response = response;
            return this;
        }

        private boolean isErrorResponsesIncludeMessageProperty() {
            boolean isInclude = true;
            for (ApiProblemDetail each : errorResponses) {
                if (Objects.isNull(each.getProperties())) {
                    isInclude = false;
                    log.error("ProblemDetails must have properties: {}", each);
                    break;
                }
                if (Objects.isNull(each.getProperties().get(MESSAGE_KEY))) {
                    isInclude = false;
                    log.error("ProblemDetails must have message property: {}", each);
                    break;
                }
                if (Objects.isNull(each.getProperties().get(ERROR_CODE_KEY))) {
                    isInclude = false;
                    log.error("ProblemDetails must have message property: {}", each);
                    break;
                }
            }
            return isInclude;
        }

        public GenericApiResponse<T> build() {
            if (Objects.isNull(httpStatusCode)) {
                throw new RuntimeException("HttpStatusCode can not be empty");
            }

            if (!errorResponses.isEmpty() && isSuccess) {
                throw new RuntimeException("If there is an error, isSuccess must be false");
            }

            if (!errorResponses.isEmpty() && !isErrorResponsesIncludeMessageProperty()) {
                throw new RuntimeException("ProblemDetails must include message property");
            }

            return new GenericApiResponse<>(response, isSuccess, errorResponses, httpStatusCode, httpStatusCode.value());
        }
    }
}
