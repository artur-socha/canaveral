package pl.codewise.canaveral.core.runtime;

import pl.codewise.canaveral.core.bean.inject.InjectMock;

@ConfigureRunnerWith(configuration = FullRunnerConfigurationProvider.class, reinitialize = true)
public class ReinitializedRunnerConfigurationTestClass {

    @InjectMock
    private DummyMockProvider mockProvider;
}
