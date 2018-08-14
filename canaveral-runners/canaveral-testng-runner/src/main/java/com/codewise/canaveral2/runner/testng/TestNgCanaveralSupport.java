package com.codewise.canaveral2.runner.testng;

import com.codewise.canaveral2.core.runtime.Runner;
import com.codewise.canaveral2.core.runtime.TestInstanceHelper;
import org.testng.annotations.BeforeClass;

public interface TestNgCanaveralSupport
{
    Runner RUNNER = Runner.instance();

    @BeforeClass(alwaysRun = true)
    default void configureInstance() {
        TestInstanceHelper testInstanceHelper = RUNNER.configureRunnerForTest(this.getClass());
        testInstanceHelper.initializeTestInstance(this);
    }
}
