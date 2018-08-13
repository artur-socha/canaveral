package com.ffb.canaveral2.runner.testng;

import com.ffb.canaveral2.core.runtime.Runner;
import com.ffb.canaveral2.core.runtime.TestInstanceHelper;
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
