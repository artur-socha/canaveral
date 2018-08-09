package com.ffb.canaveral2.core.runtime;

import com.ffb.canaveral2.core.mock.MockConfig;
import com.ffb.canaveral2.core.mock.SimpleMockProvider;
import com.ffb.canaveral2.core.util.PropertyHelper;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

class DummyMockProvider extends SimpleMockProvider implements LifeCycleListener
{

    private DummyMockProvider(String name) {
        super(name);
    }

    final AtomicReference<RunnerContext> cacheReference = new AtomicReference<>();
    final AtomicBoolean calledAfterAllMocksCreated = new AtomicBoolean(false);

    @Override
    public void initialize(RunnerContext context) {
        PropertyHelper.setProperty("com.test.property", "test");
        PropertyHelper.setProperties(Collections.singleton("com.test.app.property"), "true");
        PropertyHelper.setProperties(
                Arrays.asList(
                        "com.test.mock.port.property",
                        "com.test.mock2.port.property"),
                2931);
        context.register(this);
    }

    @Override
    public void stop() throws Exception {

    }

    @Override
    public void afterAllMocksCreated(RunnerContext runnerContext)
    {
        cacheReference.compareAndSet(null, runnerContext);
        calledAfterAllMocksCreated.set(true);
    }

    static MockConfig<DummyMockProvider> newConfig() {
        return DummyMockProvider::new;
    }
}
