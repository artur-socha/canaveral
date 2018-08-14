package com.codewise.samples.spring.it.configuration;

import com.codewise.canaveral2.core.ApplicationProvider;

import java.util.Set;

public class NoopFeatureToggleManager implements ApplicationProvider.FeatureToggleManager {

    @Override
    public void enableFeatureToggles(Set<Enum> featureToggles) {

    }

    @Override
    public void reset() {

    }

    @Override
    public boolean isFeatureEnabled(Enum featureToggle) {
        return false;
    }

    @Override
    public void enableFeatureToggle(Enum featureToggle) {

    }

    @Override
    public boolean clearFeature(Enum featureToggle) {
        return false;
    }
}
