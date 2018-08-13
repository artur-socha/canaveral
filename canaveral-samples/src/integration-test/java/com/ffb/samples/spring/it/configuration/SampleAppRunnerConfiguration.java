package com.ffb.samples.spring.it.configuration;

import com.ffb.canaveral2.addon.spring.provider.SpringBootApplicationProvider;
import com.ffb.canaveral2.addon.spring.provider.SpringTestContextProvider;
import com.ffb.canaveral2.core.runtime.ProgressAssertion;
import com.ffb.canaveral2.core.runtime.RunnerConfiguration;
import com.ffb.canaveral2.core.runtime.RunnerConfigurationProvider;
import com.ffb.canaveral2.mock.http.HttpNoDepsMockProvider;
import com.ffb.canaveral2.mock.http.Method;
import com.ffb.canaveral2.mock.http.Mime;
import com.ffb.canaveral2.mock.http.MockRuleProvider;
import com.ffb.samples.spring.SampleApp;
import com.google.common.collect.ImmutableMap;

import static java.util.Collections.emptyList;

public class SampleAppRunnerConfiguration implements RunnerConfigurationProvider {

    @Override
    public RunnerConfiguration configure() {
        return RunnerConfiguration.builder()
                .registerRandomPortUnder("server.port")
                .withSystemProperty("com.ffb.rest.client.default.timeout", "300")
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
                                .registerEndpointUnder("com.ffb.downstream.query.endpoint")
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
