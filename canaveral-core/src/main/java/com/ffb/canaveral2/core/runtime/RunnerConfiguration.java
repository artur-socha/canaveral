package com.ffb.canaveral2.core.runtime;

import com.ffb.canaveral2.core.ApplicationProvider;
import com.ffb.canaveral2.core.TestContextProvider;
import com.ffb.canaveral2.core.mock.MockConfig;
import com.ffb.canaveral2.core.mock.MockProvider;
import com.google.common.base.CaseFormat;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class RunnerConfiguration {

    private final ApplicationProvider applicationProvider;
    private final TestContextProvider testContextProvider;
    private final MockProvidersConfiguration mockProvidersConfiguration;
    private final Properties systemProperties;
    private final Set<String> randomPortsProperty;

    private RunnerConfiguration(
            ApplicationProvider applicationProvider,
            TestContextProvider testContextProvider,
            MockProvidersConfiguration mockProvidersConfiguration,
            Properties systemProperties,
            Set<String> randomPortsProperty)
    {
        this.applicationProvider = applicationProvider;
        this.testContextProvider = testContextProvider;
        this.mockProvidersConfiguration = mockProvidersConfiguration;
        this.systemProperties = systemProperties;
        this.randomPortsProperty = randomPortsProperty;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static MockBuilder mocksBuilder() {
        return new MockBuilder();
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
                .add("applicationProvider", applicationProvider)
                .add("testContextProvider", testContextProvider)
                .add("mockProvidersConfiguration", mockProvidersConfiguration)
                .add("systemProperties", systemProperties)
                .add("randomPortsProperty", randomPortsProperty)
                .toString();
    }

    public ApplicationProvider getApplicationProvider()
    {
        return applicationProvider;
    }

    public TestContextProvider getTestContextProvider()
    {
        return testContextProvider;
    }

    public MockProvidersConfiguration getMockProvidersConfiguration()
    {
        return mockProvidersConfiguration;
    }

    public Properties getSystemProperties() {
        return systemProperties;
    }


    public Set<String> getRandomPortsProperty() {
        return randomPortsProperty;
    }

    @FunctionalInterface
    interface MockProviderCreator
    {

        void consume(String ref, MockProvider provider) throws Exception;
    }

    public static class MockProvidersConfiguration
    {

        private final Map<String, MockProvider> providers;

        private MockProvidersConfiguration(Map<String, MockProvider> providers)
        {
            this.providers = providers;
        }

        public Set<String> getRefs() {
            return ImmutableSet.copyOf(providers.keySet());
        }

        public MockProvider get(String ref) {
            MockProvider mockProvider = providers.get(ref);
            Preconditions.checkNotNull(mockProvider, "Provider for " + ref + " does not exist.");

            return mockProvider;
        }

        public void forEach(MockProviderCreator creator) throws Exception {
            for (Map.Entry<String, MockProvider> entry : providers.entrySet()) {
                creator.consume(entry.getKey(), entry.getValue());
            }
        }
    }

    public static class Builder {

        private ApplicationProvider applicationProvider;
        private TestContextProvider testContextProvider;
        private MockProvidersConfiguration mockProvidersConfiguration;
        private Properties systemProperties = new Properties();
        private Set<String> randomPortsProperty = new HashSet<>();

        private Builder() {
        }

        public Builder withApplicationProvider(ApplicationProvider applicationProvider)
        {
            this.applicationProvider = applicationProvider;
            return this;
        }

        public Builder withTestConfigurationProvider(TestContextProvider testContextProvider)
        {
            this.testContextProvider = testContextProvider;
            return this;
        }

        public Builder withMocks(MockBuilder mockBuilder) {
            mockProvidersConfiguration = mockBuilder.build();
            return this;
        }

        public Builder withSystemProperty(String property, String value)
        {
            systemProperties.setProperty(property, value);
            return this;
        }

        public Builder registerRandomPortUnder(String property) {
            randomPortsProperty.add(property);
            return this;
        }

        public RunnerConfiguration build() {
            return new RunnerConfiguration(applicationProvider, testContextProvider, mockProvidersConfiguration,
                                           systemProperties, randomPortsProperty);
        }
    }

    public static class MockBuilder {

        private final Map<String, MockProvider> providers;

        private MockBuilder() {
            this.providers = new HashMap<>();
        }

        public MockBuilder provideMock(MockConfig<? extends MockProvider> config) {
            String mockName = config.getClass().getSimpleName().replace("Config", "").replace("Mock", "");
            mockName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, mockName) + ".mock.server";

            return provideMock(mockName, config);
        }

        public MockBuilder provideMock(String mockRef, MockConfig<? extends MockProvider> config) {
            Preconditions.checkArgument(!providers.containsKey(mockRef), mockRef + " was already defined.");
            providers.put(mockRef, config.build(mockRef));

            return this;
        }

        public MockProvidersConfiguration build()
        {
            return new MockProvidersConfiguration(providers);
        }
    }
}
