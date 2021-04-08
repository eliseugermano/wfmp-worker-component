package br.health.workflow.core.communication.config;

import lombok.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;

@Data
public class RestTemplateConfig {

    /**
     * Required Attributes
     */
    private HttpHeaders httpHeaders;
    private Object body;
    private String apiHost;
    private String path;
    private HttpMethod httpMethod;
    private Boolean singleReturn;

    /**
     * Not required Attributes
     */
    private MultiValueMap<String, String> queryParams;
    private HashMap<String, String> pathVariables;
    private Boolean pageable = Boolean.FALSE;

    public RestTemplateConfig(HttpHeaders httpHeaders, Object body, String apiHost, String path, HttpMethod httpMethod,
                              Boolean singleReturn) {
        this.httpHeaders = httpHeaders;
        this.body = body;
        this.apiHost = apiHost;
        this.path = path;
        this.httpMethod = httpMethod;
        this.singleReturn = singleReturn;
    }
}
