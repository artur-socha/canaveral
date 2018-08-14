package com.codewise.canaveral2.runner.testng;

import com.codewise.canaveral2.core.runtime.RunnerConfiguration;
import com.codewise.canaveral2.core.runtime.RunnerConfigurationProvider;

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
