package pl.codewise.canaveral.core.mock;

import pl.codewise.canaveral.core.runtime.RunnerContext;

public abstract class SimpleMockProvider implements MockProvider {

    private final String name;
    private int port;
    private String host;

    public SimpleMockProvider(String name) {
        this.name = name;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public String getMockName() {
        return name;
    }

    @Override
    public String getEndpoint() {
        return "http://" + getHost() + ":" + getPort();
    }

    @Override
    public void start(RunnerContext context) throws Exception {
        this.port = context.getFreePort();
        this.host = "localhost";

        initialize(context);
    }

    protected abstract void initialize(RunnerContext context);
}
