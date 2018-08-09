/*
 * Software is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.
 *
 * The Initial Developer of the Original Code is Paweł Kamiński.
 * All Rights Reserved.
 */
package com.ffb.canaveral2.runner.junit5;

import com.ffb.canaveral2.core.runtime.Runner;
import com.ffb.canaveral2.core.runtime.TestInstanceHelper;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

public class JUnit5CanaveralRunner implements BeforeAllCallback, TestInstancePostProcessor
{
    private static final Namespace IT_EXTENSION = Namespace.create(new Object());
    private static final String IT_RUNTIME = "IT_RUNTIME";

    @Override
    public void beforeAll(ExtensionContext context)
    {
        Runner runner = getStore(context).get(IT_RUNTIME, Runner.class);
        if (runner == null)
        {
            runner = Runner.instance();
            getStore(context).put(IT_RUNTIME, runner);
        }
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context)
    {
        Runner runner = getStore(context).get(IT_RUNTIME, Runner.class);
        TestInstanceHelper testInstanceHelper = runner.configureRunnerForTest(testInstance.getClass());
        testInstanceHelper.initializeTestInstance(testInstance);
    }

    private ExtensionContext.Store getStore(ExtensionContext context)
    {
        return context
                .getRoot()
                .getStore(IT_EXTENSION);
    }
}
    
