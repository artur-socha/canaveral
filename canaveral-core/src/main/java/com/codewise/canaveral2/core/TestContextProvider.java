package com.codewise.canaveral2.core;

import com.codewise.canaveral2.core.bean.BeanProvider;
import com.codewise.canaveral2.core.runtime.ProgressAssertion;
import com.codewise.canaveral2.core.runtime.RunnerContext;

public interface TestContextProvider extends BeanProvider, ProgressAssertion
{
    void initialize(RunnerContext context);

    String getProperty(String propertyKey, String defaultValue);

    void clean();
}
