package com.codewise.samples.spring.it;

import com.codewise.canaveral2.core.bean.inject.InjectMock;
import com.codewise.canaveral2.core.bean.inject.InjectTestBean;
import com.codewise.canaveral2.core.runtime.ConfigureRunnerWith;
import com.codewise.canaveral2.mock.http.HttpNoDepsMockProvider;
import com.codewise.canaveral2.runner.junit5.JUnit5CanaveralRunner;
import com.codewise.samples.spring.client.RestClient;
import com.codewise.samples.spring.it.configuration.SampleAppRunnerConfiguration;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JUnit5CanaveralRunner.class)
@ConfigureRunnerWith(configuration = SampleAppRunnerConfiguration.class)
abstract class BaseIT {

    @InjectMock
    HttpNoDepsMockProvider httpMockProvider;

    @InjectTestBean
    RestClient restClient;
}
