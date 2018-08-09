package com.ffb.canaveral2.core.runtime;

import com.ffb.canaveral2.core.bean.inject.InjectMock;

@ConfigureRunnerWith(configuration = MinimalRunnerConfigurationProvider.class)
public class MinimalRunnerConfigurationTestClass {

    @InjectMock
    private DummyMockProvider mockProvider;
}
