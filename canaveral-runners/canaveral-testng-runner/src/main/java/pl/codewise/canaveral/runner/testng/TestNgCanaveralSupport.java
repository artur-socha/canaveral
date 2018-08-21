package pl.codewise.canaveral.runner.testng;

import org.testng.annotations.BeforeClass;
import pl.codewise.canaveral.core.runtime.Runner;
import pl.codewise.canaveral.core.runtime.TestInstanceHelper;

public interface TestNgCanaveralSupport {

    Runner RUNNER = Runner.instance();

    @BeforeClass(alwaysRun = true)
    default void configureInstance() {
        TestInstanceHelper testInstanceHelper = RUNNER.configureRunnerForTest(this.getClass());
        testInstanceHelper.initializeTestInstance(this);
    }
}
