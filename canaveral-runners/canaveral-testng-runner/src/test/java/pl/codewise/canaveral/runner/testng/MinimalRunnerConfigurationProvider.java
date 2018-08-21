package pl.codewise.canaveral.runner.testng;

import pl.codewise.canaveral.core.runtime.RunnerConfiguration;
import pl.codewise.canaveral.core.runtime.RunnerConfigurationProvider;

public class MinimalRunnerConfigurationProvider implements RunnerConfigurationProvider {

    @Override
    public RunnerConfiguration configure() {
        return RunnerConfiguration.builder()
                .withSystemProperty("default.service.property", "ok")
                .withSystemProperty("env", "test")
                .withMocks(RunnerConfiguration.mocksBuilder()
                        .provideMock(DummyMockProvider.newConfig()))
                .build();
    }
}
