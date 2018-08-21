package pl.codewise.canaveral.core.runtime;

import pl.codewise.canaveral.core.bean.inject.InjectMock;

@ConfigureRunnerWith(configuration = MinimalRunnerConfigurationProvider.class)
public class MinimalRunnerConfigurationTestClass {

    @InjectMock
    private DummyMockProvider mockProvider;
}
