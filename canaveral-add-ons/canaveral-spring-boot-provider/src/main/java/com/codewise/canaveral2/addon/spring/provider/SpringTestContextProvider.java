/*
 * Software is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.
 *
 * The Initial Developer of the Original Code is Paweł Kamiński.
 * All Rights Reserved.
 */
package com.codewise.canaveral2.addon.spring.provider;

import com.codewise.canaveral2.core.ApplicationProvider.FeatureToggleManager;
import com.codewise.canaveral2.core.TestContextProvider;
import com.codewise.canaveral2.core.runtime.ProgressAssertion;
import com.codewise.canaveral2.core.runtime.RunnerContext;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SpringTestContextProvider implements TestContextProvider {

    private static final Logger log = LoggerFactory.getLogger(SpringTestContextProvider.class);
    private final Optional<String> testPropertyFile;
    private final List<Class<?>> configurations;
    private final ProgressAssertion progressAssertion;
    private AnnotationConfigApplicationContext springContext;

    private SpringTestContextProvider(ProgressAssertion progressAssertion,
            Optional<String> testPropertyFile,
            Set<Class<?>> configurations) {
        this.progressAssertion = progressAssertion;
        this.testPropertyFile = testPropertyFile;
        this.configurations = ImmutableList.copyOf(configurations);
    }

    public static Builder setUp() {
        return new Builder();
    }

    /**
     * creates separate test context that has access to all application beans. Application context is used as parent
     * context.
     *
     * @param context runner context
     */
    @Override
    public void initialize(RunnerContext context) {
        FeatureToggleManager featureToggleManager = context.getApplicationProvider().getFeatureToggleManager();
        log.info("Configuring test context with {}.", configurations);

        PropertySourcesPlaceholderConfigurer systemPropertyConfigurer = getSystemPropertyConfigurer();
        ApplicationContext applicationContext = getApplicationSpringContext(context);
        PropertySourcesPlaceholderConfigurer propertyConfigurer =
                getLocationPropertyConfigurer(testPropertyFile, applicationContext);
        try {
            springContext = new AnnotationConfigApplicationContext();
            springContext.setParent(applicationContext);
            DefaultListableBeanFactory beanFactory = springContext.getDefaultListableBeanFactory();
            beanFactory.registerSingleton("systemPropertySourcesPlaceholderConfigurer", systemPropertyConfigurer);
            beanFactory.registerSingleton("propertySourcesPlaceholderConfigurer", propertyConfigurer);
            beanFactory.registerSingleton("featureToggleManager", featureToggleManager);

            springContext.register(configurations.toArray(new Class<?>[0]));
            springContext.refresh();
        } catch (Exception e) {
            throw new IllegalStateException("Could not create test context from " + configurations, e);
        }
    }

    private ApplicationContext getApplicationSpringContext(RunnerContext context) {
        if (context.getApplicationProvider() instanceof SpringContextProvider) {
            return ((SpringContextProvider) context.getApplicationProvider()).getSpringApplicationContext();
        } else {
            throw new IllegalStateException("This provider depends on " +
                    "com.codewise.canaveral2.addon.spring.provider.SpringContextProvider. " +
                    "You cannot use this provider with different ApplicationProvider " +
                    "implementation. Sorry :(");
        }
    }

    @Override
    public String getProperty(String propertyKey, String defaultValue) {
        return springContext.getEnvironment().getProperty(propertyKey, defaultValue);
    }

    @Override
    public void clean() {
        springContext.close();
    }

    @Override
    public Object findBeanOrThrow(Class<?> beanClass, Set<Annotation> knownAnnotations) {
        return SpringBeanProviderHelper.getBean(beanClass, knownAnnotations, springContext);
    }

    @Override
    public boolean canProceed(RunnerContext runnerContext) {
        if (progressAssertion != ProgressAssertion.CAN_PROGRESS_ASSERTION) {
            return progressAssertion.canProceed(runnerContext);
        }

        return true;
    }

    private PropertySourcesPlaceholderConfigurer getLocationPropertyConfigurer(Optional<String> propertiesPath,
            ApplicationContext applicationContext) {
        return propertiesPath
                .map(propertiesFile -> {
                    PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
                    configurer.setLocalOverride(true);
                    String location = "classpath:" + propertiesFile;
                    configurer.setLocation(new DefaultResourceLoader().getResource(location));
                    return configurer;
                })
                .orElseGet(() -> applicationContext.getBean(PropertySourcesPlaceholderConfigurer.class));
    }

    private PropertySourcesPlaceholderConfigurer getSystemPropertyConfigurer() {
        PropertySourcesPlaceholderConfigurer systemPropertyConfigurer = new PropertySourcesPlaceholderConfigurer();
        systemPropertyConfigurer.setIgnoreResourceNotFound(true);
        systemPropertyConfigurer.setIgnoreUnresolvablePlaceholders(true);
        systemPropertyConfigurer.setPropertySources(new StandardEnvironment().getPropertySources());
        return systemPropertyConfigurer;
    }

    public static class Builder {

        private Optional<String> testPropertyFile = Optional.empty();
        private Set<Class<?>> configurations = new HashSet<>();
        private ProgressAssertion progressAssertion = CAN_PROGRESS_ASSERTION;

        public Builder withTestPropertyFile(String propertyFileResource) {
            this.testPropertyFile = Optional.of(propertyFileResource);
            return this;
        }

        public Builder addConfigurationFile(Class<?> configuration) {
            this.configurations.add(configuration);
            return this;
        }

        public Builder withProgAssertion(ProgressAssertion assertion) {
            this.progressAssertion = assertion;
            return this;
        }

        public TestContextProvider build() {
            Preconditions.checkArgument(!configurations.isEmpty(), "There is no spring configuration file!");

            return new SpringTestContextProvider(progressAssertion, testPropertyFile, configurations);
        }
    }
}
    
