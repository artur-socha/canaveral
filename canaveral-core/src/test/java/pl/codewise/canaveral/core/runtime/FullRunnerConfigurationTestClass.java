package pl.codewise.canaveral.core.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import pl.codewise.canaveral.core.bean.inject.InjectMock;
import pl.codewise.canaveral.core.bean.inject.InjectTestBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.Clock;

@ConfigureRunnerWith(configuration = FullRunnerConfigurationProvider.class)
class FullRunnerConfigurationTestClass {

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
