package pl.codewise.canaveral.mock.gripmock;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.codewise.canaveral.core.runtime.RunnerContext;

@ExtendWith(MockitoExtension.class)
class GripMockProviderTest {

    private GripMockProvider provider;

    @Mock
    private RunnerContext runnerContext;

    @AfterEach
    void tearDown() {
        if (provider != null) {
            provider.stop();
        }
    }

    @Test
        // Requires running docker daemon. It doesn't work on CI. Can be used to check changes locally.
        // @Disabled
    void shouldStartGripmockProvider() {
        // given
        provider = GripMockProvider.newConfig()
                .withGripMockDockerImage("ahmadmuzakki/gripmock")
                .withProtosDir("/testprotos")
                .withProtoFile("test.proto")
                .withProtoFile("test2.proto")
                .build("gripmock");

        // when
        provider.start(runnerContext);

        // then
        // no timeout Exception on provider.start(..)
    }
}