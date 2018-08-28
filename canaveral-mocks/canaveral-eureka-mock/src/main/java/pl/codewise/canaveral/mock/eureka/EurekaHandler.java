package pl.codewise.canaveral.mock.eureka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.net.MediaType;
import com.netflix.appinfo.DataCenterInfo;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.LeaseInfo;
import com.netflix.appinfo.MyDataCenterInfo;
import com.netflix.discovery.converters.jackson.EurekaJsonJacksonCodec;
import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.Applications;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.singletonList;

class EurekaHandler implements HttpHandler {

    private static final Logger log = LoggerFactory.getLogger(EurekaHandler.class);
    private AtomicLong versionDelta = new AtomicLong(0L);

    private final List<Application> staticApplications = new CopyOnWriteArrayList<>();
    private final Map<String, Application> lazyApplications = new ConcurrentHashMap<>();
    private final Pattern pathPattern;
    private final ObjectMapper objectMapper;

    EurekaHandler(String pathToMock) {
        pathPattern = Pattern.compile(pathToMock + ".*");
        objectMapper = new ObjectMapper();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        log.debug("Trying to handle a response for {} {}", exchange.getRequestMethod(), exchange.getRequestURI());

        String incomingPath = exchange.getRequestURI().getPath();

        if (pathPattern.matcher(incomingPath).matches()) {
            if (exchange.getRequestMethod().equals("GET")) {
                respondWith(exchange, toJson(new Applications("HS", versionDelta.get(), getAllApplications())));
                return;
            } else if (exchange.getRequestMethod().equals("PUT")) {
                Set<Map.Entry<String, Application>> appIds = lazyApplications.entrySet();
                InstanceInfoWrapper instanceInfo = findAppForHeartbeat(incomingPath, appIds);
                if (instanceInfo == null) {
                    String message = "Could not find appID in " + appIds;
                    respondWith(exchange, message.getBytes(), 404, MediaType.PLAIN_TEXT_UTF_8);
                } else {
                    String body = objectMapper.writerFor(InstanceInfoWrapper.class)
                            .writeValueAsString(instanceInfo);
                    respondWith(exchange, body);
                }
                return;
            } else if (exchange.getRequestMethod().equals("POST")) {
                try {
                    InstanceInfoWrapper instanceInfo = objectMapper.readValue(exchange.getRequestBody(),
                            InstanceInfoWrapper.class);
                    InstanceInfo instance = instanceInfo.getInstance();
                    versionDelta.incrementAndGet();

                    String appName = instance.getAppName();

                    log.info("New app {} was registered with {}:{} - status {}.",
                            appName, instance.getIPAddr(), instance.getPort(), instance.getStatus());
                    lazyApplications.put(appName, createApplication(instance));

                    respondWithNoContent(exchange);

                    return;
                } catch (Exception e) {
                    log.error("Error while handling request", e);
                }
            } else if (exchange.getRequestMethod().equals("DELETE")) {
                respondWith(exchange, 200);
                return;
            }
        }

        log.warn("Could not match request to {} /{}", exchange.getRequestMethod(), incomingPath);

        String message = "Could not find resource under " + exchange.getRequestMethod() + " /" + exchange
                .getRequestURI().getPath();
        respondWith(exchange, message.getBytes(), 404, MediaType.PLAIN_TEXT_UTF_8);
    }

    private InstanceInfoWrapper findAppForHeartbeat(String incomingPath, Set<Map.Entry<String, Application>> appIds) {
        return appIds.stream()
                .filter(entry -> incomingPath.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .flatMap(application -> application.getInstances().stream())
                .findFirst()
                .map(instanceInfo -> {
                    InstanceInfoWrapper wrapper = new InstanceInfoWrapper();
                    wrapper.setInstance(instanceInfo);
                    return wrapper;
                })
                .orElse(null);
    }

    void setStaticApplications(List<Application> staticApplications) {
        this.staticApplications.addAll(staticApplications);
    }

    List<Application> getStaticApplications() {
        return staticApplications;
    }

    Collection<Application> getLazyApplications() {
        return ImmutableList.copyOf(lazyApplications.values());
    }

    List<Application> getAllApplications() {
        checkNotNull(staticApplications, "Eureka mock is not ready yet.");
        return ImmutableList.<Application>builder()
                .addAll(staticApplications)
                .addAll(getLazyApplications())
                .build();
    }

    private void respondWith(HttpExchange exchange, String body) {
        respondWith(exchange, body.getBytes(), 200, MediaType.JSON_UTF_8);
    }

    private void respondWith(HttpExchange exchange, byte[] body, int statusCode, MediaType mediaType) {
        try {
            exchange.getResponseHeaders().set("Content-Type", mediaType.toString());
            exchange.sendResponseHeaders(statusCode, body.length);
            if (body.length > 0) {
                OutputStream responseBody = exchange.getResponseBody();
                responseBody.write(body);
                responseBody.close();
            }
        } catch (IOException e) {
            log.error("Could not send response :/ " + exchange.getRequestURI().getPath(), e);
        }
        exchange.close();
    }

    private void respondWith(HttpExchange ctx, int status) {
        respondWith(ctx, new byte[0], status, MediaType.PLAIN_TEXT_UTF_8);
    }

    private void respondWithNoContent(HttpExchange ctx) {
        respondWith(ctx, 204);
    }

    private Application createApplication(InstanceInfo instance) {
        String appName = instance.getAppName();
        log.debug("Registering eureka app {} with {}.", appName, instance);
        return new Application(appName, singletonList(instance));
    }

    Application createApplication(String appName, int port, String host) {
        return new Application(appName, ImmutableList.of(instanceInfo(appName, port, host)));
    }

    static InstanceInfo instanceInfo(String instanceId, String appName, int port, String host) {
        long nowTimestamp = Instant.now().toEpochMilli();
        return new InstanceInfo(
                instanceId,
                appName,
                "",
                host,
                "",
                new InstanceInfo.PortWrapper(true, port),
                null,
                "",
                "",
                "",
                "",
                appName,
                "",
                -1,
                new MyDataCenterInfo(DataCenterInfo.Name.MyOwn),
                host,
                InstanceInfo.InstanceStatus.UP,
                null,
                LeaseInfo.Builder.newBuilder()
                        .setRenewalTimestamp(nowTimestamp)
                        .setServiceUpTimestamp(nowTimestamp)
                        .build(),
                false,
                null,
                nowTimestamp,
                nowTimestamp,
                null,
                "");
    }

    static InstanceInfo instanceInfo(String appName, int port, String host) {
        return instanceInfo(UUID.randomUUID().toString(), appName, port, host);
    }

    private static String toJson(Applications apps) {
        try {
            return new EurekaJsonJacksonCodec().getObjectMapper(Applications.class).writeValueAsString(apps);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not transform apps to json.", e);
        }
    }

    @VisibleForTesting
    static class InstanceInfoWrapper {

        private InstanceInfo instance;

        public InstanceInfo getInstance() {
            return instance;
        }

        public void setInstance(InstanceInfo instance) {
            this.instance = instance;
        }
    }
}
