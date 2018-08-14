package com.codewise.canaveral2.runner.junit4;

import com.codewise.canaveral2.core.runtime.Runner;
import com.codewise.canaveral2.core.runtime.TestInstanceHelper;
import junitparams.JUnitParamsRunner;
import org.junit.runners.model.InitializationError;

public class JUnit4CanaveralRunner extends JUnitParamsRunner
{

    private static final Runner RUNNER = Runner.instance();

    private final TestInstanceHelper testInstanceHelper;

    /**
     * Creates a BlockJUnit4ClassRunner to run {@code klass}
     *
     * @throws InitializationError if the test class is malformed.
     */
    public JUnit4CanaveralRunner(Class<?> klass) throws InitializationError
    {
        super(klass);
        testInstanceHelper = RUNNER.configureRunnerForTest(klass);
    }

    @Override
    protected Object createTest() throws Exception
    {
        Object testInstance = super.createTest();
        return testInstanceHelper.initializeTestInstance(testInstance);
    }
}
