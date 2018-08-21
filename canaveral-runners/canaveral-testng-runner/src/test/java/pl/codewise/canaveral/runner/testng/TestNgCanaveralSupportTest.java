package pl.codewise.canaveral.runner.testng;

import org.assertj.core.api.Assertions;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import pl.codewise.canaveral.core.bean.inject.InjectMock;
import pl.codewise.canaveral.core.runtime.ConfigureRunnerWith;

@ConfigureRunnerWith(configuration = MinimalRunnerConfigurationProvider.class)
public class TestNgCanaveralSupportTest implements TestNgCanaveralSupport {

    @InjectMock
    private DummyMockProvider mock;
    private String property;

    @Factory(dataProvider = "properties")
    TestNgCanaveralSupportTest(String property) {

        this.property = property;
    }

    @DataProvider
    public static Object[][] properties() {
        return new Object[][] {
                {"A"}, {"B"}
        };
    }

    @Test
    public void testShouldInitializeMock() {
        Assertions.assertThat(mock).isNotNull();
        Assertions.assertThat(property).isNotNull();
    }
}