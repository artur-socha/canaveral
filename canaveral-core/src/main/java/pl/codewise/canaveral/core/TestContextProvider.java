package pl.codewise.canaveral.core;

import pl.codewise.canaveral.core.bean.BeanProvider;
import pl.codewise.canaveral.core.runtime.ProgressAssertion;
import pl.codewise.canaveral.core.runtime.RunnerContext;

public interface TestContextProvider extends BeanProvider, ProgressAssertion {

    void initialize(RunnerContext context);

    String getProperty(String propertyKey, String defaultValue);

    void clean();
}
