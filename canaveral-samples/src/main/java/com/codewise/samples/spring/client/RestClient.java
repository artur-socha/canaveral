package com.codewise.samples.spring.client;

import com.codewise.samples.spring.webapp.rest.model.Response;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

public class RestClient {

    private final RestTemplate client;
    private String endpoint;

    public RestClient(RestTemplate client, String endpoint) {
        this.client = client;
        this.endpoint = endpoint;
    }

    public Response askGoogle(String query) {
        URI uri = UriComponentsBuilder.fromUriString(endpoint + "/v1/google")
                .queryParam("q", query)
                .build()
                .toUri();
        return client.getForObject(uri, Response.class);
    }
}
