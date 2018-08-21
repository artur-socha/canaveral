package pl.codewise.canaveral.core.runtime;

import org.mockito.Mockito;

public class MockedRunnerConfigurationProvider implements RunnerConfigurationProvider {

    static final RunnerConfiguration configurationMock = Mockito.mock(RunnerConfiguration.class);

    @Override
    public RunnerConfiguration configure() {
        return configurationMock;
    }
}
