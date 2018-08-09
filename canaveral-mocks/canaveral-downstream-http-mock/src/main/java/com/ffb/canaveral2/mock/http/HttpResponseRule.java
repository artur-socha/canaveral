package com.ffb.canaveral2.mock.http;

import com.google.common.base.MoreObjects;

import java.util.List;
import java.util.Map;

class HttpResponseRule
{

    private final byte[] body;
    private final Mime contentType;
    private final StatusCode status;
    private final Map<String, List<String>> headers;

    HttpResponseRule(byte[] body, Mime contentType, StatusCode status, Map<String, List<String>> headers)
    {
        this.body = body;
        this.contentType = contentType;
        this.status = status;
        this.headers = headers;
    }

    byte[] getBody()
    {
        return body;
    }

    Mime getContentType()
    {
        return contentType;
    }

    StatusCode getStatus()
    {
        return status;
    }

    Map<String, List<String>> getHeaders()
    {
        return headers;
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
                .add("body", body)
                .add("contentType", contentType)
                .add("status", status)
                .add("headers", headers)
                .toString();
    }
}
