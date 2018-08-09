package com.ffb.canaveral2.core.mock;

import com.ffb.canaveral2.core.runtime.RunnerContext;

public interface MockProvider
{

    int getPort();

    String getHost();

    String getEndpoint();

    String getMockName();

    void start(RunnerContext context) throws Exception;

    void stop() throws Exception;
}
