package pl.codewise.canaveral.core.runtime;

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
