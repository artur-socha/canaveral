package com.ffb.canaveral2.core.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ffb.canaveral2.core.bean.inject.InjectMock;
import com.ffb.canaveral2.core.bean.inject.InjectTestBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.Clock;

@ConfigureRunnerWith(configuration = FullRunnerConfigurationProvider.class)
class FullRunnerConfigurationTestClass
{

    @Inject
    Clock clock;

    @InjectMock("first")
    DummyMockProvider mockProvider;

    @InjectMock("OtherDummyMock")
    DummyMockProvider otherDummyMockProvider;

    @Inject
    @Named("customBeanQualifier")
    ObjectMapper mapper;

    @InjectTestBean
    @Named("otherBeanQualifier")
    Clock testClock;
}
