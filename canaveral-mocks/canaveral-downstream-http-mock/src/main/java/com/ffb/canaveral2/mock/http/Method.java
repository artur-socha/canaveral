package com.ffb.canaveral2.mock.http;

public enum Method
{
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE"),
    HEAD("HEAD");

    private final String method;

    Method(String method)
    {
        this.method = method;
    }

    public String getMethod()
    {
        return method;
    }
}
