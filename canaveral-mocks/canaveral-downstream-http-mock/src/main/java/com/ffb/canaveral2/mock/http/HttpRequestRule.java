package com.ffb.canaveral2.mock.http;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

class HttpRequestRule
{

    private final Map<String, List<String>> headers;
    private final String pathPattern;
    private final Method method;
    private final Map<String, List<String>> query;
    private final byte[] body;

    HttpRequestRule(Method method, String pathPattern, Map<String, List<String>> query, Map<String, List<String>> headers,
                    byte[] body)
    {
        this.headers = headers;
        this.pathPattern = pathPattern;
        this.method = method;
        this.query = query;
        this.body = body;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        HttpRequestRule that = (HttpRequestRule) o;
        return Objects.equal(headers, that.headers) &&
               Objects.equal(pathPattern, that.pathPattern) &&
               method == that.method &&
               Objects.equal(query, that.query) &&
               Arrays.equals(body, that.body);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(headers, pathPattern, method, query, body);
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
                .add("headers", headers)
                .add("pathPattern", pathPattern)
                .add("method", method)
                .add("query", query)
                .add("body", body)
                .toString();
    }

    public Map<String, List<String>> getHeaders()
    {
        return headers;
    }

    String getPathPattern()
    {
        return pathPattern;
    }

    Method getMethod()
    {
        return method;
    }

    Map<String, List<String>> getQuery()
    {
        return query;
    }

    byte[] getBody()
    {
        return body;
    }
}
