# Sample Spring app with integration tests

Purpose of this module is to give a brief introduction to a canaveral2. This sample uses junit5 runner.

There are 3 key elements.
1. Spring boot application ```com.ffb.samples.spring.SampleApp```
1. ```com.ffb.samples.spring.it.configuration.SampleAppRunnerConfiguration``` that configures runner.
1. Test ```com.ffb.samples.spring.it.RestClientIT``` itself that is annotated with runner config.

## Configuration

#### SpringBootApplicationProvider
```com.ffb.canaveral2.addon.spring.provider.SpringBootApplicationProvider``` requires base class of spring's boot application. 

It is not required but is good practice to provide custom implementation of ```com.ffb.canaveral2.core.ApplicationProvider.FeatureToggleManager``` 
which will alter application's behaviour when you switch toggles back and forth. 

Sometimes it also good to check whether application has started and is fully initialized. For those purposes use your 
custom implementation of ```com.ffb.canaveral2.core.runtime.ProgressAssertion```. For example such assertion could check 
that application has registered itself in discovery service mock.

#### SpringTestContextProvider
```com.ffb.canaveral2.addon.spring.provider.SpringTestContextProvider``` configures all beans that are required by test
but you don't necessary want to define them in application context. In general canaveral tries to separate application's and test's bean contexts from each other.
Good example here is a configuration of ```com.ffb.samples.spring.client.RestClient``` to simulate calls from external service to application under test.

Test context can also be configured with separate properties ```/application-test.properties``` and is using spring's bean configuration.

## Test cases
Test cases are simply unit test. Canaveral makes sure each test is run after all mocks and application/test context is initialized so you don't have to worry about that.
Canaveral will inject any bean that is annotated with ```com.ffb.canaveral2.core.bean.inject.InjectMock```, ```com.ffb.canaveral2.core.bean.inject.InjectTestBean``` and
```javax.inject.Inject```. 

Application beans are injected using default ```javax.inject.Inject```.
Spring's annotation ```org.springframework.beans.factory.annotation.Autowired``` may also be used. 
This functionality is supported only by application provider ```com.ffb.canaveral2.addon.spring.provider.SpringBootApplicationProvider``` 
and may not by available if you configure runner with different application provider's implementation.

Each test case must be annotated with
```
@ExtendWith(JUnit5CanaveralRunner.class)
@ConfigureRunnerWith(configuration = SampleAppRunnerConfiguration.class)
class RestClientIT {
 ...
}
```
but those may be extracted to common base class so you don't repeat initialization and beans injection in every test.