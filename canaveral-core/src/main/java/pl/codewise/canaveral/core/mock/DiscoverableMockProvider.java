package pl.codewise.canaveral.core.mock;

import java.util.Set;

public interface DiscoverableMockProvider extends MockProvider {

    Set<String> getDiscoverableAppNames();

    boolean wantsRegistration();
}
