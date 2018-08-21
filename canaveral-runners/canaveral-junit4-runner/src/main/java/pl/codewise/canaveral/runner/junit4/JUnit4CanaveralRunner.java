package pl.codewise.canaveral.runner.junit4;

import junitparams.JUnitParamsRunner;
import org.junit.runners.model.InitializationError;
import pl.codewise.canaveral.core.runtime.Runner;
import pl.codewise.canaveral.core.runtime.TestInstanceHelper;

public class JUnit4CanaveralRunner extends JUnitParamsRunner {

    private static final Runner RUNNER = Runner.instance();

    private final TestInstanceHelper testInstanceHelper;

    /**
     * Creates a BlockJUnit4ClassRunner to run {@code klass}
     *
     * @throws InitializationError if the test class is malformed.
     */
    public JUnit4CanaveralRunner(Class<?> klass) throws InitializationError {
        super(klass);
        testInstanceHelper = RUNNER.configureRunnerForTest(klass);
    }

    @Override
    protected Object createTest() throws Exception {
        Object testInstance = super.createTest();
        return testInstanceHelper.initializeTestInstance(testInstance);
    }
}
