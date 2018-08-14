package com.codewise.canaveral2.core.runtime;

import com.codewise.canaveral2.core.bean.inject.InjectMock;

@ConfigureRunnerWith(configuration = MinimalRunnerConfigurationProvider.class)
public class MinimalRunnerConfigurationTestClass {

    @InjectMock
    private DummyMockProvider mockProvider;
}
