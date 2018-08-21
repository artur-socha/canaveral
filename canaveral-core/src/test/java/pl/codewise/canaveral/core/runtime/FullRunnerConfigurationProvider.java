package pl.codewise.canaveral.core.runtime;

import pl.codewise.canaveral.core.ApplicationProvider;
import pl.codewise.canaveral.core.TestContextProvider;

import static org.mockito.Mockito.mock;

public class FullRunnerConfigurationProvider implements RunnerConfigurationProvider {

    static final ApplicationProvider applicationProviderMock = mock(ApplicationProvider.class, "application mock");
    static final TestContextProvider testContextMock = mock(TestContextProvider.class, "test context mock");

    @Override
    public RunnerConfiguration configure() {
        return RunnerConfiguration.builder()
                .withSystemProperty("default.service.property", "ok")
                .registerRandomPortUnder("app.port")
                .withApplicationProvider(applicationProviderMock)
                .withTestConfigurationProvider(testContextMock)
                .withMocks(RunnerConfiguration.mocksBuilder()
                        .provideMock("first", DummyMockProvider.newConfig())
                        .provideMock("OtherDummyMock", DummyMockProvider.newConfig()))
                .build();
    }
}
