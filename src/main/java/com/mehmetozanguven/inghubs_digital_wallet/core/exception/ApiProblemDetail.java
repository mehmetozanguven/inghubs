package com.mehmetozanguven.inghubs_digital_wallet.core.exception;

import com.mehmetozanguven.inghubs_digital_wallet.core.GenericApiResponse;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatusCode;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Getter
@Setter
public class ApiProblemDetail {
    private String title;
    private int status;
    private String detail;
    private URI instance;
    private Map<String, Object> properties;

    public static ApiProblemDetail forStatus(HttpStatusCode status) {
        ApiProblemDetail apiProblemDetail = new ApiProblemDetail();
        apiProblemDetail.setStatus(status.value());
        return apiProblemDetail;
    }

    public static ApiProblemDetail forException(ApiException apiException, URI instance) {
        ApiProblemDetail problemDetail = new ApiProblemDetail();
        problemDetail.setExceptionError(apiException);
        problemDetail.setInstance(instance);
        return problemDetail;
    }

    public ApiProblemDetail() {
        this.properties = new HashMap<>();
        this.instance = URI.create("");
    }

    public void setExceptionError(ApiException apiException) {
        if (Objects.isNull(apiException.getExceptionMessage())) {
            throw new IllegalArgumentException("ExceptionMessage can't be null");
        }
        if (Objects.isNull(apiException.getCode())) {
            throw new IllegalArgumentException("Code can't be null");
        }
        ApiExceptionInfo apiExceptionInfo = apiException.getStarterExceptionInformation();

        this.status = apiExceptionInfo.getExceptionStatus().value();
        this.title = apiExceptionInfo.getMessage();
        this.properties.put(GenericApiResponse.MESSAGE_KEY, apiExceptionInfo.getMessage());
        this.properties.put(GenericApiResponse.ERROR_CODE_KEY, apiExceptionInfo.getCode());
    }

    public void setProperty(String key, Object value) {
        this.properties.put(key, value);
    }
}
