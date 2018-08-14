package com.codewise.samples.spring.webapp.rest.model;

import org.springframework.http.HttpStatus;

public class Response {

    private int status;
    private String body;

    public Response() {
    }

    public Response(HttpStatus statusCode, String body) {
        status = statusCode.value();
        this.body = body;
    }

    public int getStatus() {
        return status;
    }

    public String getBody() {
        return body;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
