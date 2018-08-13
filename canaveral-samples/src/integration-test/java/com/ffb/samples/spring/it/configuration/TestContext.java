package com.ffb.samples.spring.it.configuration;

import com.ffb.canaveral2.core.ApplicationProvider;
import com.ffb.canaveral2.core.bean.inject.InjectTestBean;
import com.ffb.samples.spring.client.RestClient;
import com.ffb.samples.spring.configuartion.RestTemplateConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;

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
    private ApplicationProvider.FeatureToggleManager featureToggleManager;

    @Bean
    public RestClient restClient(
            @Value("${server.port:}") int applicationPort,
            RestTemplate testRestTemplate) {
        return new RestClient(testRestTemplate, "http://localhost:" + applicationPort);
    }
}
