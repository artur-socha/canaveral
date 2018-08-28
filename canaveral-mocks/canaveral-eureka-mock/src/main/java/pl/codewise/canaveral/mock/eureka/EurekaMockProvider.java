package pl.codewise.canaveral.mock.eureka;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.shared.Application;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.codewise.canaveral.core.mock.DiscoverableMockProvider;
import pl.codewise.canaveral.core.mock.MockConfig;
import pl.codewise.canaveral.core.mock.MockProvider;
import pl.codewise.canaveral.core.runtime.RunnerContext;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static pl.codewise.canaveral.mock.eureka.EurekaHandler.instanceInfo;

public class EurekaMockProvider implements MockProvider {

    private static final Logger log = LoggerFactory.getLogger(EurekaMockProvider.class);

    private final EurekaMockConfig mockConfig;
    private final String mockName;
    private int port = 0;

    private HttpServer mockServer;
    private EurekaHandler eurekaHandler;

    private EurekaMockProvider(EurekaMockConfig mockConfig, String mockName) {
        this.mockConfig = mockConfig;
        this.mockName = mockName;
    }

    public static EurekaMockConfig newConfig() {
        return new EurekaMockConfig();
    }

    @Override
    public int getPort() {
        checkArgument(port != 0, "Mock is not started yet!");
        return port;
    }

    @Override
    public String getHost() {
        return MockConfig.HOST;
    }

    @Override
    public String getEndpoint() {
        return "http://" + getHost() + ":" + getPort() + "/eureka";
    }

    @Override
    public String getMockName() {
        return mockName;
    }

    @Override
    public void start(RunnerContext context) throws Exception {
        this.port = context.getFreePort();
        eurekaHandler = new EurekaHandler(mockConfig.pathToMock);

        List<Application> staticRegisteredApplications =
                mockConfig.registeredApplications.entrySet().stream()
                        .map(entry -> {
                            String appName = entry.getKey();
                            List<InstanceInfo> instances = entry.getValue().stream()
                                    .map(instance -> instanceInfo(instance.id, appName, instance.port, instance.host))
                                    .collect(Collectors.toList());
                            return new Application(appName, instances);
                        })
                        .collect(Collectors.toList());
        eurekaHandler.setStaticApplications(staticRegisteredApplications);

        mockServer = HttpServer.create(new InetSocketAddress(port), 0);
        mockServer.createContext("/", eurekaHandler);
        mockServer.start();

        if (mockConfig.enableLazyRegistration) {
            System.setProperty("eureka.instance.ipAddress", "localhost");
            System.setProperty("eureka.instance.hostname", "localhost");
            System.setProperty("eureka.client.registration.enabled", "true");
        }

        System.setProperty(mockConfig.endpointProperty, getEndpoint());
    }

    public void allMocksCreated(RunnerContext cache) {
        List<Application> staticApplicationList = cache.getMocks()
                .filter(provider -> provider instanceof DiscoverableMockProvider)
                .map(provider -> (DiscoverableMockProvider) provider)
                .filter(provider -> {
                    boolean wantsRegistration = provider.wantsRegistration();
                    if (!wantsRegistration) {
                        log.info("Discoverable mock {} resigned from registration.", provider.getMockName());
                    }
                    return wantsRegistration;
                })
                .flatMap(provider -> provider.getDiscoverableAppNames().stream()
                        .map(name -> createApplication(name, provider)))
                .collect(Collectors.toList());

        Map<String, List<Application>> applicationsByName =
                staticApplicationList.stream().collect(Collectors.groupingBy(Application::getName));

        List<Application> aggregatedStaticApplicationList = groupApplicationsByName(applicationsByName);

        eurekaHandler.setStaticApplications(aggregatedStaticApplicationList);
    }

    @Override
    public void stop() throws Exception {
        mockServer.stop(0);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("mockName", mockName)
                .toString();
    }

    public Collection<Application> getAllApplications() {
        checkNotNull(eurekaHandler, "Eureka mock is not ready yet.");
        return eurekaHandler.getAllApplications();
    }

    public Collection<Application> getStaticApplications() {
        checkNotNull(eurekaHandler, "Eureka mock is not ready yet.");
        return eurekaHandler.getStaticApplications();
    }

    public Collection<Application> getLazyApplications() {
        checkNotNull(eurekaHandler, "Eureka mock is not ready yet.");
        return eurekaHandler.getLazyApplications();
    }

    private List<Application> groupApplicationsByName(Map<String, List<Application>> applicationsByName) {
        return applicationsByName.entrySet().stream()
                .map(entry ->
                        entry.getValue().stream().reduce((app1, app2) -> {
                            app2.getInstances().forEach(app1::addInstance);
                            return app1;
                        }))
                .map(maybeApp -> maybeApp.orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Application createApplication(String appName, MockProvider mock) {
        log.info("Mocking eureka app {} with endpoint {}.", appName, mock.getEndpoint());
        return eurekaHandler.createApplication(appName, mock.getPort(), mock.getHost());
    }

    public static class EurekaMockConfig implements MockConfig<EurekaMockProvider> {

        private final Map<String, Set<InstanceConfig>> registeredApplications = new LinkedHashMap<>();
        private String endpointProperty;
        private String pathToMock;
        private boolean enableLazyRegistration;

        private EurekaMockConfig() {
        }

        @Override
        public EurekaMockProvider build(String mockName) {
            return new EurekaMockProvider(this, mockName);
        }

        public EurekaMockConfig registerEndpointUnder(String property) {
            endpointProperty = property;
            return this;
        }

        public EurekaMockConfig mockJsonResponseFor(String pathToMock) {
            this.pathToMock = pathToMock;
            return this;
        }

        public EurekaMockConfig enableEnableLazyAppsRegistration(boolean enableLazyRegistration) {
            this.enableLazyRegistration = enableLazyRegistration;
            return this;
        }

        public EurekaMockConfig registerApplication(String appName, InstanceConfig... instances) {
            registeredApplications.compute(appName, (_appName, _instances) -> {
                if (_instances == null) {
                    _instances = Sets.newLinkedHashSet();
                }
                _instances.addAll(Lists.newArrayList(instances));
                return _instances;
            });

            return this;
        }
    }

    public static class InstanceConfig {

        public static final int DEFAULT_PORT = 18943;
        public final String id;
        public final String host;
        public final int port;

        private InstanceConfig(String id, String host, int port) {
            this.id = id;
            this.host = host;
            this.port = port;
        }

        public static InstanceConfig newInstance(String host) {
            return newInstance(UUID.randomUUID().toString(), host);
        }

        public static InstanceConfig newInstance(String id, String host) {
            return newInstance(id, host, DEFAULT_PORT);
        }

        public static InstanceConfig newInstance(String id, String host, int port) {
            return new InstanceConfig(id, host, port);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            InstanceConfig that = (InstanceConfig) o;
            return port == that.port &&
                    Objects.equals(id, that.id) &&
                    Objects.equals(host, that.host);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, host, port);
        }
    }
}
