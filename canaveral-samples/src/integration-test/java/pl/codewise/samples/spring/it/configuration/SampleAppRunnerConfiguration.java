package pl.codewise.samples.spring.it.configuration;

import com.google.common.collect.ImmutableMap;
import pl.codewise.canaveral.addon.spring.provider.SpringBootApplicationProvider;
import pl.codewise.canaveral.addon.spring.provider.SpringTestContextProvider;
import pl.codewise.canaveral.core.runtime.ProgressAssertion;
import pl.codewise.canaveral.core.runtime.RunnerConfiguration;
import pl.codewise.canaveral.core.runtime.RunnerConfigurationProvider;
import pl.codewise.canaveral.mock.http.HttpNoDepsMockProvider;
import pl.codewise.canaveral.mock.http.Method;
import pl.codewise.canaveral.mock.http.Mime;
import pl.codewise.canaveral.mock.http.MockRuleProvider;
import pl.codewise.samples.spring.SampleApp;

import static java.util.Collections.emptyList;

public class SampleAppRunnerConfiguration implements RunnerConfigurationProvider {

    @Override
    public RunnerConfiguration configure() {
        return RunnerConfiguration.builder()
                .registerRandomPortUnder("server.port")
                .withSystemProperty("pl.codewise.rest.client.default.timeout", "300")
                .withApplicationProvider(new SpringBootApplicationProvider(
                        SampleApp.class,
                        new NoopFeatureToggleManager(),
                        ProgressAssertion.CAN_PROGRESS_ASSERTION))
                .withTestConfigurationProvider(SpringTestContextProvider.setUp()
                        .withTestPropertyFile("/application-test.properties")
                        .withProgAssertion(new ClientCanConnectProgressAssertion())
                        .addConfigurationFile(TestContext.class)
                        .build())
                .withMocks(RunnerConfiguration.mocksBuilder()
                        .provideMock(HttpNoDepsMockProvider.newConfig()
                                .registerEndpointUnder("pl.codewise.downstream.query.endpoint")
                                .withRules(provider -> provider
                                        .whenCalledWith(Method.GET, "/search")
                                        .thenRespondWith(MockRuleProvider.Body.from(
                                                ImmutableMap.of("content", emptyList()),
                                                Mime.JSON))
                                )
                        ))
                .build();
    }
}
