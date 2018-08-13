package com.ffb.samples.spring;

import com.ffb.samples.spring.configuartion.AppConfig;
import org.apache.catalina.authenticator.jaspic.AuthConfigFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Import;

import javax.security.auth.message.config.AuthConfigFactory;

@Import(AppConfig.class)
public class SampleApp implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SampleApp.class);

    static {
        if (AuthConfigFactory.getFactory() == null) {
            AuthConfigFactory.setFactory(new AuthConfigFactoryImpl());
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(SampleApp.class, args);
    }

    @Override
    public void run(String... strings) {
        log.info("Starting Sample Spring Boot App");
    }
}
