package pl.codewise.samples.spring.it.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;
import pl.codewise.canaveral.core.ApplicationProvider;
import pl.codewise.canaveral.core.bean.inject.InjectTestBean;
import pl.codewise.samples.spring.client.RestClient;
import pl.codewise.samples.spring.configuartion.RestTemplateConfiguration;

/**
 * independent spring context configuration
 */
@Configuration
@Import({
        RestTemplateConfiguration.class
})
public class TestContext {

    /**
     * Note: Sample app has no feature toggles and Dummy {@link NoopFeatureToggleManager} is installed in configuration.
     * This demonstrate how configured feature manager can be injected as mock bean into test via {@link InjectTestBean}
     */
    @Autowired
    private ApplicationProvider.FeatureToggleManager<Void> featureToggleManager;

    @Bean
    public RestClient restClient(
            @Value("${server.port:}") int applicationPort,
            RestTemplate testRestTemplate) {
        return new RestClient(testRestTemplate, "http://localhost:" + applicationPort);
    }
}
