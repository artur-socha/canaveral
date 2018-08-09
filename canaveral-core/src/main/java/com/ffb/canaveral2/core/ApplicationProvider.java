package com.ffb.canaveral2.core;

import com.ffb.canaveral2.core.bean.BeanManager;
import com.ffb.canaveral2.core.bean.BeanProvider;
import com.ffb.canaveral2.core.runtime.ProgressAssertion;
import com.ffb.canaveral2.core.runtime.RunnerContext;

import java.util.Properties;
import java.util.Set;

public interface ApplicationProvider extends BeanProvider, BeanManager, ProgressAssertion
{

    boolean isInitialized();

    Properties getProperties();

    FeatureToggleManager getFeatureToggleManager();

    void start(RunnerContext runnerContext);

    void clean();

    int getPort();

    String getEndpoint();

    interface FeatureToggleManager
    {
        void enableFeatureToggles(Set<Enum> featureToggles);

        void reset();

        boolean isFeatureEnabled(Enum featureToggle);

        void enableFeatureToggle(Enum featureToggle);

        boolean clearFeature(Enum featureToggle);
    }
}
