package pl.codewise.canaveral.mock.http;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.sun.net.httpserver.HttpExchange;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpRawRequest {

    private final String path;
    private final URI originalUri;
    private final Map<String, List<String>> queryParams;
    private final Map<String, List<String>> headers;
    private final Method method;
    private final byte[] body;

    private HttpRawRequest(HttpExchange request, byte[] body) {
        this.path = request.getRequestURI().getPath();
        this.originalUri = request.getRequestURI();
        this.queryParams = queryStringToMultiValueMap(request.getRequestURI().getQuery());
        this.headers = request.getRequestHeaders();
        this.method = Method.valueOf(request.getRequestMethod());
        this.body = body;
    }

    static HttpRawRequest from(HttpExchange request, byte[] body) {
        return new HttpRawRequest(request, body);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("path", path)
                .add("originalUri", originalUri)
                .add("queryParams", queryParams)
                .add("headers", headers)
                .add("method", method)
                .add("body", body)
                .toString();
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public List<Header> getHeadersAsList() {
        return getHeaders().entrySet().stream()
                .flatMap(entry -> entry.getValue().stream().map(value -> new Header(entry.getKey(), value)))
                .collect(Collectors.toList());
    }

    public String getPath() {
        return path;
    }

    public URI getOriginalUri() {
        return originalUri;
    }

    public Method getMethod() {
        return method;
    }

    public Map<String, List<String>> getQueryParams() {
        return queryParams;
    }

    public List<QueryParam> getQueryParamsAsList() {
        return queryParams.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream().map(value -> new QueryParam(entry.getKey(), value)))
                .collect(Collectors.toList());
    }

    public byte[] getBody() {
        return body;
    }

    private Map<String, List<String>> queryStringToMultiValueMap(String query) {
        if (query == null) {
            return Collections.emptyMap();
        }
        Map<String, List<String>> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");

            String queryKey = entry[0];
            String queryValue = "";
            if (entry.length > 1) {
                queryValue = entry[1];
            }

            result
                    .computeIfAbsent(queryKey, (k) -> new ArrayList<>())
                    .add(queryValue);
        }
        return ImmutableMap.copyOf(result);
    }

    public static class Header {

        private final String key;
        private final String value;

        private Header(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public static Header from(String key, String value) {
            return new Header(key, value);
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Header that = (Header) o;
            return Objects.equal(key, that.key) &&
                    Objects.equal(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(key, value);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("key", key)
                    .add("value", value)
                    .toString();
        }
    }

    public static class QueryParam {

        private final String key;
        private final String value;

        private QueryParam(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public static QueryParam from(String key, String value) {
            return new QueryParam(key, value);
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            QueryParam that = (QueryParam) o;
            return Objects.equal(key, that.key) &&
                    Objects.equal(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(key, value);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("key", key)
                    .add("value", value)
                    .toString();
        }
    }
}
