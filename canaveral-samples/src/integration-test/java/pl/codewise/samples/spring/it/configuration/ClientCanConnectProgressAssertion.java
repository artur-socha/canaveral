package pl.codewise.samples.spring.it.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.codewise.canaveral.core.runtime.ProgressAssertion;
import pl.codewise.canaveral.core.runtime.RunnerContext;
import pl.codewise.samples.spring.client.RestClient;

import java.util.Collections;

public class ClientCanConnectProgressAssertion implements ProgressAssertion {

    private static final Logger log = LoggerFactory.getLogger(ClientCanConnectProgressAssertion.class);

    @Override
    public boolean canProceed(RunnerContext runnerContext) {
        String luckyNumber = runnerContext.getApplicationProvider().getProperty("pl.codewise.luckyNumber", "0");
        log.info("Application properties are available. Lucky number is {}.", luckyNumber);

        log.info("Making a call to application.");
        RestClient client = (RestClient) runnerContext.getTestBean(RestClient.class, Collections.emptySet());
        return client.askGoogle("any").getStatus() == 200;
    }
}
