package pl.codewise.canaveral.mock.gripmock;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import pl.codewise.canaveral.core.mock.MockConfig;
import pl.codewise.canaveral.core.mock.MockProvider;
import pl.codewise.canaveral.core.runtime.RunnerContext;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

public class GripMockProvider implements MockProvider {

    public static class GripMockConfig implements MockConfig<GripMockProvider> {

        private String image = "ahmadmuzakki/gripmock";
        private String protosDir;
        private Set<String> protoFiles = Sets.newLinkedHashSet();

        @Override
        public GripMockProvider build(String mockName) {
            return new GripMockProvider(this, mockName);
        }

        public GripMockConfig withGripMockDockerImage(String image) {
            this.image = image;
            return this;
        }

        public GripMockConfig withProtoFile(String fileName) {
            this.protoFiles.add(fileName);
            return this;
        }

        public GripMockConfig withProtosDir(String protosDir) {
            this.protosDir = protosDir;
            return this;
        }
    }

    private static final Logger log = LoggerFactory.getLogger(GripMockProvider.class);
    private final GripMockConfig mockConfig;
    private final String mockName;
    private int port;
    private String host;
    private GenericContainer server;

    private GripMockProvider(GripMockConfig mockConfig, String mockName) {
        this.mockConfig = mockConfig;
        this.mockName = mockName;
    }

    public static GripMockConfig newConfig() {
        return new GripMockConfig();
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public String getEndpoint() {
        return "http://" + getHost() + ":" + this.getPort();
    }

    @Override
    public String getMockName() {
        return mockName;
    }

    @Override
    public void start(RunnerContext context) {
        log.info("Starting dockerized GripMock from {}.", mockConfig.image);

        server = new GenericContainer(mockConfig.image)
                .withExposedPorts(4770, 4771)
                .withStartupTimeout(Duration.ofSeconds(5))
                .withClasspathResourceMapping(mockConfig.protosDir, "/proto", BindMode.READ_WRITE)
                .withCommand("gripmock " +
                        mockConfig.protoFiles.stream()
                                .map(file -> "/proto/" + file)
                                .collect(Collectors.joining(" "))
                );

        server.start();

        this.port = server.getFirstMappedPort();
        this.host = server.getContainerIpAddress();
    }

    @Override
    public void stop() {
        server.stop();
    }
}
