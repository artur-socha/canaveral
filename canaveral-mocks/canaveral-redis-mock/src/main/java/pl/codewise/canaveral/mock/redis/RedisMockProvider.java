package pl.codewise.canaveral.mock.redis;

import com.codewise.voluum.utils.it.MockConfig;
import com.codewise.voluum.utils.it.MockProvider;
import com.codewise.voluum.utils.it.RunnerCache;
import com.google.common.base.MoreObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.embedded.RedisServer;

public class RedisMockProvider implements MockProvider {

    private static final Logger log = LoggerFactory.getLogger(RedisMockProvider.class);

    private final RedisMockConfig mockConfig;
    private final String mockName;
    private int port;
    private RedisServer server;

    private RedisMockProvider(RedisMockConfig mockConfig, String mockName) {
        this.mockConfig = mockConfig;
        this.mockName = mockName;
    }

    public static RedisMockConfig newConfig() {
        return new RedisMockConfig();
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getHost() {
        return mockConfig.host;
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
    public void start(int port, RunnerCache cache) throws Exception {
        this.port = port;

        String key = mockConfig.hostProperty;
        System.setProperty(key, getHost());
        log.info("Setting system property {} to {}.", key, System.getProperty(key));
        key = mockConfig.portProperty;
        System.setProperty(key, Integer.toString(getPort()));
        log.info("Setting system property {} to {}.", key, System.getProperty(key));

        server = new RedisServer(port);
        reset();
    }

    @Override
    public void stop() {
        server.stop();
    }

    public RedisMockProvider reset() {
        if (server.isActive()) {
            server.stop();
        }
        server.start();
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("mockName", mockName)
                .toString();
    }

    public static class RedisMockConfig implements MockConfig<RedisMockProvider> {

        private String host = HOST;
        private String hostProperty;
        private String portProperty;

        private RedisMockConfig() {
        }

        @Override
        public RedisMockProvider build(String mockName) {
            return new RedisMockProvider(this, mockName);
        }

        public RedisMockConfig registerHostUnder(String key) {
            hostProperty = key;
            return this;
        }

        public RedisMockConfig registerPortUnder(String key) {
            portProperty = key;
            return this;
        }
    }
}
