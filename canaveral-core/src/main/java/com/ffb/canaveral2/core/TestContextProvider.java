package com.ffb.canaveral2.core;

import com.ffb.canaveral2.core.bean.BeanProvider;
import com.ffb.canaveral2.core.runtime.ProgressAssertion;
import com.ffb.canaveral2.core.runtime.RunnerContext;

import java.util.Properties;

public interface TestContextProvider extends BeanProvider, ProgressAssertion
{
    void initialize(RunnerContext context);

    Properties getProperties();

    void clean();
}
