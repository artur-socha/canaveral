/*
 * Software is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.
 *
 * The Initial Developer of the Original Code is Paweł Kamiński.
 * All Rights Reserved.
 */
package com.ffb.canaveral2.addon.spring.provider;

import com.ffb.canaveral2.core.ApplicationProvider;
import com.ffb.canaveral2.core.runtime.ProgressAssertion;
import com.ffb.canaveral2.core.runtime.RunnerContext;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Properties;
import java.util.Set;


public class SpringBootApplicationProvider implements ApplicationProvider, SpringContextProvider
{
    private static final Logger log = LoggerFactory.getLogger(SpringBootApplicationProvider.class);

    private final Class<?> springBaseClass;
    private final ProgressAssertion progressAssertion;
    private final FeatureToggleManager featureToggleManager;

    private ConfigurableApplicationContext springContext;
    private int port;

    public SpringBootApplicationProvider(Class<?> springBaseClass,
                                         FeatureToggleManager featureToggleManager,
                                         ProgressAssertion progressAssertion)
    {
        Preconditions.checkNotNull(springBaseClass, "Cannot initialize from null base class.");
        Preconditions.checkNotNull(featureToggleManager, "Cannot initialize without null feature toggle manger.");

        this.springBaseClass = springBaseClass;
        this.featureToggleManager = featureToggleManager;
        this.progressAssertion = progressAssertion;
    }

    @Override
    public boolean isInitialized()
    {
        return springContext.isActive();
    }

    @Override
    public Properties getProperties()
    {
        try
        {
            return springContext.getBean(PropertiesFactoryBean.class).getObject();
        }
        catch (IOException e)
        {
            log.error("Could not access properties :/", e);
            return null;
        }
    }

    @Override
    public FeatureToggleManager getFeatureToggleManager()
    {
        return featureToggleManager;
    }

    @Override
    public void start(RunnerContext runnerContext)
    {
        port = runnerContext.getFreePort();
        System.setProperty("server.port", Integer.toString(port));

        SpringApplication application = new SpringApplication(springBaseClass);
        application
                .addListeners(event -> log.debug("Got Application event. {}", event));
        application.setRegisterShutdownHook(false);
        springContext = application.run();
    }

    @Override
    public void clean()
    {
        springContext.close();
    }

    @Override
    public int getPort()
    {
        return port;
    }

    @Override
    public String getEndpoint()
    {
        return "http://localhost:" + getPort();
    }

    @Override
    public Object findBeanOrThrow(Class<?> beanClass, Set<Annotation> knownAnnotations)
    {
        return SpringBeanProviderHelper.getBean(beanClass, knownAnnotations, springContext);
    }

    /**
     * this method is useful when test runner should wait for some state to change outside this initialization.
     * for example when application needs to register in some discovery service.
     *
     * @param runnerContext of the test
     * @return whether this application is started and fully initialized. by default true;
     */
    @Override
    public boolean canProceed(RunnerContext runnerContext)
    {
        if (progressAssertion != ProgressAssertion.CAN_PROGRESS_ASSERTION)
        {
            return progressAssertion.canProceed(runnerContext);
        }

        return true;
    }

    @Override
    public void inject(Object instance)
    {
        AutowireCapableBeanFactory beanFactory = springContext.getAutowireCapableBeanFactory();
        beanFactory.autowireBeanProperties(instance, AutowireCapableBeanFactory.AUTOWIRE_NO, false);
        beanFactory.initializeBean(instance, instance.getClass().getName());
    }

    @Override
    public ConfigurableApplicationContext getSpringApplicationContext()
    {
        return springContext;
    }
}
    
