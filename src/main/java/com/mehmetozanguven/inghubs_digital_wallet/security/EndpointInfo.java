package com.mehmetozanguven.inghubs_digital_wallet.security;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Data
public class EndpointInfo {
    private String urlPattern;
    private List<HttpMethod> httpMethods;

    public EndpointInfo(String urlPattern, List<HttpMethod> httpMethods) {
        this.urlPattern = urlPattern;
        this.httpMethods = httpMethods;
    }

    public static class Builder {
        private String urlPattern;
        private final List<HttpMethod> httpMethods;

        public Builder() {
            this.httpMethods = new ArrayList<>();
        }

        public Builder setUrlPattern(String urlPattern) {
            this.urlPattern = urlPattern;
            return this;
        }

        public Builder allowAllHttpMethods() {
            this.httpMethods.addAll(Arrays.asList(HttpMethod.values()));
            return this;
        }

        public Builder addAllowedHttpMethod(HttpMethod httpMethod) {
            this.httpMethods.add(httpMethod);
            return this;
        }

        public EndpointInfo build() {
            if (this.httpMethods.isEmpty()) {
                throw new RuntimeException("Allowed http method can't be null");
            }
            if (StringUtils.isBlank(this.urlPattern)) {
                throw new RuntimeException("Url pattern can't be null");
            }
            return new EndpointInfo(this.urlPattern, this.httpMethods);
        }
    }

    public static List<PathPatternRequestMatcher> toPathPatternRequest(EndpointInfo endpointInfo) {
        List<PathPatternRequestMatcher> antPathRequestMatchers = new ArrayList<>();
        for (HttpMethod eachMethod: endpointInfo.getHttpMethods()) {
            antPathRequestMatchers.add(PathPatternRequestMatcher.withDefaults().matcher(eachMethod, endpointInfo.getUrlPattern()));
        }
        return antPathRequestMatchers;
    }


    public static List<PathPatternRequestMatcher> toPathPatternRequests(List<EndpointInfo> endpointInfos) {
        return endpointInfos.stream().flatMap(
                endpointInfo -> toPathPatternRequest(endpointInfo).stream()
        ).collect(Collectors.toList());
    }

    public static List<EndpointInfo> ALLOWED_ENDPOINTS() {
        return List.of(
                new EndpointInfo
                        .Builder()
                        .setUrlPattern("/api/auth/customer/create")
                        .addAllowedHttpMethod(HttpMethod.POST)
                        .build(),
                new EndpointInfo
                        .Builder()
                        .setUrlPattern("/api/auth/employee/create")
                        .addAllowedHttpMethod(HttpMethod.POST)
                        .build(),
                new EndpointInfo
                        .Builder()
                        .setUrlPattern("/api/auth/login")
                        .addAllowedHttpMethod(HttpMethod.POST)
                        .build(),
                new EndpointInfo
                        .Builder()
                        .setUrlPattern("/api-docs**")
                        .addAllowedHttpMethod(HttpMethod.GET)
                        .build(),
                new EndpointInfo
                        .Builder()
                        .setUrlPattern("/v3/api-docs")
                        .addAllowedHttpMethod(HttpMethod.GET)
                        .build(),
                new EndpointInfo
                        .Builder()
                        .setUrlPattern("/v3/api-docs/**")
                        .addAllowedHttpMethod(HttpMethod.GET)
                        .build(),
                new EndpointInfo
                        .Builder()
                        .setUrlPattern("/swagger-ui**")
                        .addAllowedHttpMethod(HttpMethod.GET)
                        .build(),
                new EndpointInfo
                        .Builder()
                        .setUrlPattern("/swagger-ui/**")
                        .addAllowedHttpMethod(HttpMethod.GET)
                        .build(),
                new EndpointInfo
                        .Builder()
                        .setUrlPattern("/swagger-ui/index.html")
                        .addAllowedHttpMethod(HttpMethod.GET)
                        .build());
    }

}
