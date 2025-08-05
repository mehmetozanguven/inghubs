package com.mehmetozanguven.inghubs_digital_wallet.core;


import com.mehmetozanguven.inghubs_digital_wallet.core.exception.ApiErrorInfo;
import com.mehmetozanguven.inghubs_digital_wallet.core.exception.ApiException;
import com.mehmetozanguven.inghubs_digital_wallet.core.exception.ApiProblemDetail;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ApiRestControllerAdvice extends ResponseEntityExceptionHandler {
    private static final String SOMETHING_WENT_WRONG = "Something went wrong";

    private final MessageSource messageSource;

    protected String tryToGetRequestUriFromWebRequest(WebRequest webRequest) {
        try {
            return ((ServletWebRequest) webRequest).getRequest().getRequestURI();
        } catch (Exception ex) {
            log.info("Can not get request uri from web request. Returning contextPath ...", ex);
            return webRequest.getContextPath();
        }
    }


    @ExceptionHandler(value = ConstraintViolationException.class)
    public ResponseEntity<Object> constraintViolationException(ConstraintViolationException ex, WebRequest request) {
        log.error("ConstraintViolationException for the following url :: {}", tryToGetRequestUriFromWebRequest(request), ex);

        URI instance = URI.create(tryToGetRequestUriFromWebRequest(request));
        ApiException apiException = new ApiException(ApiErrorInfo.CONSTRAINT_VIOLATION_ERROR);
        ApiProblemDetail problemDetail = ApiProblemDetail.forException(apiException, instance);


        GenericApiResponse<String> response = new GenericApiResponse.Builder<String>()
                .isSuccess(false)
                .httpStatusCode(apiException.getExceptionStatus())
                .addErrorResponse(problemDetail)
                .build();
        return new ResponseEntity<>(response, response.httpStatus);
    }


    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.error("handleMethodArgumentNotValid for the following url :: {}", tryToGetRequestUriFromWebRequest(request), ex);

        URI instance = URI.create(tryToGetRequestUriFromWebRequest(request));
        ApiException apiException = new ApiException(ApiErrorInfo.METHOD_ARGUMENT_NOT_VALID_ERROR);
        ApiProblemDetail problemDetail = ApiProblemDetail.forException(apiException, instance);

        String errorMessage = ApiErrorInfo.METHOD_ARGUMENT_NOT_VALID_ERROR.getMessage();
        List<FieldError> errors = ex.getBindingResult().getFieldErrors();
        if (CollectionUtils.isNotEmpty(errors)) {
            errorMessage = messageSource.getMessage(errors.getFirst().getCode(), errors.getFirst().getArguments(), "Validation Failed", Locale.ENGLISH);
        }
        problemDetail.setProperty(GenericApiResponse.MESSAGE_KEY, errorMessage);

        GenericApiResponse<?> response = new GenericApiResponse.Builder<>()
                .isSuccess(false)
                .httpStatusCode(apiException.getExceptionStatus())
                .addErrorResponse(problemDetail)
                .build();
        return new ResponseEntity<>(response, response.httpStatus);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        log.error("AccessDeniedException for the following url :: {}", tryToGetRequestUriFromWebRequest(request), ex);
        URI instance = URI.create(tryToGetRequestUriFromWebRequest(request));
        var exception = new ApiException(ApiErrorInfo.ACCESS_DENIED_EXCEPTION);
        ApiProblemDetail problemDetail = ApiProblemDetail.forException(exception, instance);

        GenericApiResponse<String> response = new GenericApiResponse.Builder<String>()
                .isSuccess(false)
                .httpStatusCode(exception.getExceptionStatus())
                .addErrorResponse(problemDetail)
                .build();
        return new ResponseEntity<>(response, response.httpStatus);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        log.error("Authentication for the following url :: {}", tryToGetRequestUriFromWebRequest(request), ex);
        URI instance = URI.create(tryToGetRequestUriFromWebRequest(request));
        ApiException apiException = switch (ex) {
            case BadCredentialsException e -> new ApiException(ApiErrorInfo.BAD_CREDENTIALS_EXCEPTION);
            case InsufficientAuthenticationException e -> new ApiException(ApiErrorInfo.AUTHENTICATION_EXCEPTION);
            case DisabledException e -> new ApiException(ApiErrorInfo.ACCOUNT_DISABLED_EXCEPTION);
            default -> {
                if (ex.getCause() instanceof DisabledException) {
                    yield new ApiException(ApiErrorInfo.ACCOUNT_DISABLED_EXCEPTION);
                }
                yield new ApiException(ApiErrorInfo.INVALID_REQUEST);
            }
        };


        ApiProblemDetail problemDetail = ApiProblemDetail.forException(apiException, instance);

        GenericApiResponse<String> response = new GenericApiResponse.Builder<String>()
                .isSuccess(false)
                .httpStatusCode(apiException.getExceptionStatus())
                .addErrorResponse(problemDetail)
                .build();
        return new ResponseEntity<>(response, response.httpStatus);
    }

    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<Object> unKnownException(RuntimeException ex, WebRequest request) {
        log.error("unKnown exception for the following url :: {}", tryToGetRequestUriFromWebRequest(request), ex);
        URI instance = URI.create(tryToGetRequestUriFromWebRequest(request));
        ApiException apiException = new ApiException(ApiErrorInfo.SOMETHING_WENT_WRONG);

        ApiProblemDetail problemDetail = ApiProblemDetail.forException(apiException, instance);

        GenericApiResponse<String> response = new GenericApiResponse.Builder<String>()
                .isSuccess(false)
                .httpStatusCode(apiException.getExceptionStatus())
                .addErrorResponse(problemDetail)
                .build();
        return new ResponseEntity<>(response, response.httpStatus);
    }


    @ExceptionHandler(value = ApiException.class)
    public ResponseEntity<Object> handleApiException(ApiException ex, HttpServletRequest httpServletRequest) {
        URI instance = URI.create(httpServletRequest.getRequestURI());

        ApiProblemDetail problemDetail = ApiProblemDetail.forException(ex, instance);

        GenericApiResponse<String> response = new GenericApiResponse.Builder<String>()
                .isSuccess(false)
                .httpStatusCode(ex.getExceptionStatus())
                .addErrorResponse(problemDetail)
                .build();
        return new ResponseEntity<>(response, ex.getExceptionStatus());
    }
}
