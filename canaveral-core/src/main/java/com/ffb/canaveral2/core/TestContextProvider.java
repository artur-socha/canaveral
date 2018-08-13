package com.ffb.canaveral2.core;

import com.ffb.canaveral2.core.bean.BeanProvider;
import com.ffb.canaveral2.core.runtime.ProgressAssertion;
import com.ffb.canaveral2.core.runtime.RunnerContext;

public interface TestContextProvider extends BeanProvider, ProgressAssertion
{
    void initialize(RunnerContext context);

    String getProperty(String propertyKey, String defaultValue);

    void clean();
}
