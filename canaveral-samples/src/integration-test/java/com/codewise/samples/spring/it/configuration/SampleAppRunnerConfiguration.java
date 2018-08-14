package com.codewise.samples.spring.it.configuration;

import com.codewise.canaveral2.addon.spring.provider.SpringBootApplicationProvider;
import com.codewise.canaveral2.addon.spring.provider.SpringTestContextProvider;
import com.codewise.canaveral2.core.runtime.ProgressAssertion;
import com.codewise.canaveral2.core.runtime.RunnerConfiguration;
import com.codewise.canaveral2.core.runtime.RunnerConfigurationProvider;
import com.codewise.canaveral2.mock.http.HttpNoDepsMockProvider;
import com.codewise.canaveral2.mock.http.Method;
import com.codewise.canaveral2.mock.http.Mime;
import com.codewise.canaveral2.mock.http.MockRuleProvider;
import com.codewise.samples.spring.SampleApp;
import com.google.common.collect.ImmutableMap;

import static java.util.Collections.emptyList;

public class SampleAppRunnerConfiguration implements RunnerConfigurationProvider {

    @Override
    public RunnerConfiguration configure() {
        return RunnerConfiguration.builder()
                .registerRandomPortUnder("server.port")
                .withSystemProperty("com.codewise.rest.client.default.timeout", "300")
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
                                .registerEndpointUnder("com.codewise.downstream.query.endpoint")
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
