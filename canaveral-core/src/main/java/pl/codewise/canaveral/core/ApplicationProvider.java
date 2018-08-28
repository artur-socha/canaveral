package pl.codewise.canaveral.core;

import pl.codewise.canaveral.core.bean.BeanManager;
import pl.codewise.canaveral.core.bean.BeanProvider;
import pl.codewise.canaveral.core.runtime.ProgressAssertion;
import pl.codewise.canaveral.core.runtime.RunnerContext;

import java.util.Set;

public interface ApplicationProvider extends BeanProvider, BeanManager, ProgressAssertion {

    boolean isInitialized();

    String getProperty(String propertyKey, String defaultValue);

    FeatureToggleManager getFeatureToggleManager();

    void start(RunnerContext runnerContext);

    void clean();

    int getPort();

    String getEndpoint();

    interface FeatureToggleManager<T> {

        void enableFeatureToggles(Set<T> featureToggles);

        void reset();

        boolean isFeatureEnabled(T featureToggle);

        void enableFeatureToggle(T featureToggle);

        void clearFeature(T featureToggle);
    }
}
