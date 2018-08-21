package pl.codewise.canaveral.core.mock;

import pl.codewise.canaveral.core.runtime.RunnerContext;

public interface MockProvider {

    int getPort();

    String getHost();

    String getEndpoint();

    String getMockName();

    void start(RunnerContext context) throws Exception;

    void stop() throws Exception;
}
