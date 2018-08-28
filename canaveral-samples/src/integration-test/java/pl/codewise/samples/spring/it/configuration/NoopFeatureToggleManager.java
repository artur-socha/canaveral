package pl.codewise.samples.spring.it.configuration;

import pl.codewise.canaveral.core.ApplicationProvider;

import java.util.Set;

public class NoopFeatureToggleManager implements ApplicationProvider.FeatureToggleManager<Void> {

    @Override
    public void enableFeatureToggles(Set<Void> featureToggles) {
    }

    @Override
    public void reset() {
    }

    @Override
    public boolean isFeatureEnabled(Void featureToggle) {
        return false;
    }

    @Override
    public void enableFeatureToggle(Void featureToggle) {
    }

    @Override
    public void clearFeature(Void featureToggle) {
    }
}
