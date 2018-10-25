package pl.codewise.canaveral.mock.jmx;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class JmxMockTest {

    private static final Logger log = LoggerFactory.getLogger(JmxMockTest.class);
    public static final String JSON_RESPONSE = "{a : b}";
    public static final String PLAIN_RESPONSE = "a: b";
    public static final int JMX_PORT = 1234;

    private JmxMockProvider jmxMockProvider;
    private static final String CAMPAIGN_ID = "bf86b33b-7774-4220-bcf1-59ee80665920";

    @Before
    public void setUp() throws Exception {
        jmxMockProvider = JmxMockProvider.newConfig()
                .registerJmxPortUnder("bidder.us-east.jmx.port")
                .withRules(rules -> {
                    rules.withMBean(
                            "com.codewise.voluum.dsp.metrics.service:name=inMemoryMetricsService," +
                                    "type=InMemoryMetricsService")
                            .whenCalled("getMetricsCount", CAMPAIGN_ID, "json")
                            .thenRespondWith(JSON_RESPONSE)
                            .whenCalled("getMetricsCount", CAMPAIGN_ID)
                            .thenRespondWith(PLAIN_RESPONSE)
                    ;
                    rules.withMBean(
                            "com.codewise.voluum.dsp.metrics.service:name=otherMbean," +
                                    "type=OtherMbean")
                            .whenCalled("test", new Object[] {})
                            .thenRespondWith("otherTestBeanResult");
                })
                .build("bidder-jmx");

        jmxMockProvider.start(JMX_PORT, null);
    }

    @After
    public void tearDown() throws Exception {
        jmxMockProvider.stop();
    }

    @Test
    public void shouldMatchPattern() {
        Object response =
                callJmx("com.codewise.voluum.dsp.metrics.service:name=inMemoryMetricsService," +
                                "type=InMemoryMetricsService",
                        "getMetricsCount",
                        new Object[] {CAMPAIGN_ID, "json"},
                        new String[] {String.class.getName(), String.class.getName()});
        Assert.assertEquals(JSON_RESPONSE, response);
        Object otherRuleResponse =
                callJmx("com.codewise.voluum.dsp.metrics.service:name=inMemoryMetricsService," +
                                "type=InMemoryMetricsService",
                        "getMetricsCount",
                        new Object[] {CAMPAIGN_ID},
                        new String[] {String.class.getName()});
        Assert.assertEquals(PLAIN_RESPONSE, otherRuleResponse);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenNoRuleMatched() {
        Object response =
                callJmx("com.codewise.voluum.dsp.metrics.service:name=inMemoryMetricsService," +
                                "type=InMemoryMetricsService",
                        "dd",
                        new Object[] {CAMPAIGN_ID, "json"},
                        new String[] {String.class.getName(), String.class.getName()});
        Assert.assertEquals(JSON_RESPONSE, response);
    }

    private Object callJmx(String objectName, String operationName, Object[] parameters, String[] signature) {
        try (JMXConnector connector = JMXConnectorFactory
                .connect(new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:" + JMX_PORT
                        + "/jmxrmi"))) {
            MBeanServerConnection mBeanServerConnection = connector.getMBeanServerConnection();
            log.trace("Attempting to invoke {} operation", operationName);
            Object response = mBeanServerConnection.invoke(new ObjectName(objectName), operationName, parameters,
                    signature);
            log.debug("Successfully executed {}", operationName);
            return response;
        } catch (Exception e) {
            if (e.getCause() instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) e.getCause();
            } else {
                throw new IllegalStateException("Jmx execution failed", e);
            }
        }
    }
}
