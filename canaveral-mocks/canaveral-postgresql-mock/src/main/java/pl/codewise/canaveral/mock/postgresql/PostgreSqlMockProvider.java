package pl.codewise.canaveral.mock.postgresql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.testcontainers.containers.PostgreSQLContainer;
import pl.codewise.canaveral.core.mock.MockConfig;
import pl.codewise.canaveral.core.mock.MockProvider;
import pl.codewise.canaveral.core.runtime.RunnerContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static pl.codewise.canaveral.core.util.PropertyHelper.setProperty;

public class PostgreSqlMockProvider implements MockProvider {

    private static final Logger log = LoggerFactory.getLogger(PostgreSqlMockProvider.class);

    private final String mockName;
    private final PostgresSqlMockConfig mockConfig;
    private PostgreSQLContainer container;

    private int exposedPort;
    private String endpointUrl;

    private JdbcManager jdbcManager;

    private PostgreSqlMockProvider(PostgresSqlMockConfig mockConfig, String mockName) {
        this.mockConfig = mockConfig;
        this.mockName = mockName;
    }

    public static PostgresSqlMockConfig newConfig() {
        return new PostgresSqlMockConfig();
    }

    @Override
    public int getPort() {
        return exposedPort;
    }

    @Override
    public String getHost() {
        return mockConfig.host;
    }

    @Override
    public String getEndpoint() {
        return endpointUrl;
    }

    @Override
    public String getMockName() {
        return mockName;
    }

    public JdbcManager getJdbcManager() {
        return jdbcManager;
    }

    public <T> List<T> select(String sql, RowMapper<T> rowMapper) {
        return jdbcManager.select(sql, rowMapper);
    }

    public int update(String sql) {
        return jdbcManager.update(sql);
    }

    public int update(String sql, Object... args) {
        return jdbcManager.update(sql, args);
    }

    public int delete(String sql) {
        return jdbcManager.update(sql);
    }

    public int delete(String sql, Object... args) {
        return jdbcManager.update(sql, args);
    }

    public String getUser() {
        return container.getUsername();
    }

    public String getPassword() {
        return container.getPassword();
    }

    @Override
    public void start(RunnerContext context) {
        log.info("Starting dockerized PostgreSql from {}.", mockConfig.dockerImage);

        container = new PostgreSQLContainer(mockConfig.dockerImage);
        container.withDatabaseName(mockConfig.database);

        mockConfig.envs.forEach((k, v) -> {
            log.info("Adding env property {}={}.", k, v);
            container.addEnv(k, v);
        });
        mockConfig.parameters.forEach((k, v) -> {
            log.info("Adding parameter property {}={}.", k, v);
            container.addParameter(k, v);
        });

        container.start();

        exposedPort = container.getMappedPort(5432);
        endpointUrl = container.getJdbcUrl();

        if (mockConfig.endpointProperty != null) {
            setProperty(mockConfig.endpointProperty, endpointUrl);
        }

        if (mockConfig.userNameProperty != null) {
            setProperty(mockConfig.userNameProperty, container.getUsername());
        }

        if (mockConfig.passwordProperty != null) {
            setProperty(mockConfig.passwordProperty, container.getPassword());
        }

        jdbcManager = JdbcManager.create(endpointUrl, container.getUsername(), container.getPassword());
    }

    @Override
    public void stop() {
        container.stop();
    }

    public static class PostgresSqlMockConfig implements MockConfig<PostgreSqlMockProvider> {

        private String database;
        private String host = "127.0.0.1";
        private String endpointProperty;
        private String dockerImage = "postgres:10.4";
        private String userNameProperty;
        private String passwordProperty;
        private Map<String, String> envs = new HashMap<>();
        private Map<String, String> parameters = new HashMap<>();

        @Override
        public PostgreSqlMockProvider build(String mockName) {
            return new PostgreSqlMockProvider(this, mockName);
        }

        public PostgresSqlMockConfig withDataBaseName(String database) {
            this.database = requireNonNull(database);
            return this;
        }

        public PostgresSqlMockConfig registerEndpointUnderProperty(String property) {
            this.endpointProperty = requireNonNull(property);
            return this;
        }

        public PostgresSqlMockConfig registerUsernameUnder(String property) {
            this.userNameProperty = requireNonNull(property);
            return this;
        }

        public PostgresSqlMockConfig registerPasswordUnder(String property) {
            this.passwordProperty = requireNonNull(property);
            return this;
        }

        public PostgresSqlMockConfig withDockerImage(String cassandraDockerImage) {
            this.dockerImage = requireNonNull(cassandraDockerImage);
            return this;
        }

        public PostgresSqlMockConfig withEnvironmentVariable(String key, String value) {
            String prevValue = this.envs.putIfAbsent(key, value);
            if (prevValue != null) {
                throw new IllegalStateException("You are overriding env variable " + prevValue + " for key " + key);
            }
            return this;
        }

        public PostgresSqlMockConfig withParameterVariable(String key, String value) {
            String prevValue = this.parameters.putIfAbsent(key, value);
            if (prevValue != null) {
                throw new IllegalStateException("You are overriding parameter variable " + prevValue + " for key " + key);
            }
            return this;
        }
    }
}
