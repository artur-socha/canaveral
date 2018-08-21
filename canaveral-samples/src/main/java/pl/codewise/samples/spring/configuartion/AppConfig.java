package pl.codewise.samples.spring.configuartion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import pl.codewise.samples.spring.webapp.BasePackage;

import java.time.Clock;

@Configuration
@EnableAutoConfiguration
@Import({
        MvcConfiguration.class,
        RestTemplateConfiguration.class
})
@ComponentScan(basePackageClasses = {BasePackage.class})
public class AppConfig {

    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void appInitialized(ApplicationEvent event) {
        log.info("Application was initialized");
    }
}
