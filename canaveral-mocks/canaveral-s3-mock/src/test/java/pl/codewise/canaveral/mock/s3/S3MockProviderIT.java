package pl.codewise.canaveral.mock.s3;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.codewise.canaveral.core.runtime.RunnerContext;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class S3MockProviderIT {

    private static final String INIT_BUCKET = "initBucket";
    private static final String INIT_KEY = "initFile";
    private S3MockProvider s3MockProvider;

    @Mock
    private RunnerContext runnerContext;

    private AmazonS3 s3;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(runnerContext.getFreePort()).thenCallRealMethod();

        s3MockProvider = S3MockProvider.newConfig()
                .registerEndpointUnder("foo.bar")
                .withHost("127.0.0.1")
                .put(INIT_BUCKET, INIT_KEY, "/sample.txt")
                .build("s3Mock");
        s3MockProvider.start(runnerContext);

        s3 = AmazonS3Client.builder()
                .withClientConfiguration(new ClientConfiguration().withProtocol(Protocol.HTTP))
                .withEndpointConfiguration(
                        new EndpointConfiguration(s3MockProvider.getEndpoint(), "us-east-1"))
                .build();
    }

    @AfterEach
    void tearDown() throws Exception {
        s3MockProvider.stop();
    }

    @Test
    void shouldSystemPropertyWithEndpointBeSet() {
        // when
        String endpoint = System.getProperty("foo.bar");

        // then
        assertThat(endpoint)
                .isNotNull()
                .isEqualTo(s3MockProvider.getEndpoint());
    }

    @Test
    void shouldGetInitiallyAddedFile() throws IOException {
        // when
        S3Object object = s3.getObject(INIT_BUCKET, INIT_KEY);

        // then
        assertS3Object(object, "That's a sample file.");
    }

    @Test
    void shouldRemotelyReturnLocallyAddedFile() throws IOException {
        // given
        String theContent = "aContent";
        String bucket = "aBucket";
        String key = "aFile";
        s3MockProvider.getS3Mock().put(bucket, key, theContent.getBytes());

        // when
        S3Object object = s3.getObject(bucket, key);

        // then
        assertS3Object(object, theContent);
    }

    @Test
    void shouldReturnInitFile() throws IOException {
        // when
        S3Object object = s3.getObject(INIT_BUCKET, INIT_KEY);

        // then
        assertS3Object(object, "That's a sample file.");
    }

    @Test
    void shouldListObjects() {
        // given
        String otherKey = "newKey";
        s3MockProvider.getS3Mock().put(INIT_BUCKET, otherKey, "Some".getBytes());

        // when
        ObjectListing objectListing = s3.listObjects(INIT_BUCKET);

        // then
        assertThat(objectListing).isNotNull();
        assertThat(objectListing.getObjectSummaries()).extracting("key").containsOnly(INIT_KEY, otherKey);
    }

    @Test
    void shouldAddFile() {
        // given
        String theContent = "aContent";
        String bucket = "aBucket";
        String key = "aFile";

        // when
        s3.putObject(bucket, key, theContent);

        // then
        S3MockObject object = s3MockProvider.getS3Mock().get(bucket, key);
        assertThat(object).isNotNull();
        assertThat(object.content()).isEqualTo(theContent.getBytes());
    }

    @Test
    void shouldResetToDefaults() {
        // given
        String theContent = "aContent";
        String bucket = "aBucket";
        String key = "aFile";
        s3.putObject(bucket, key, theContent);

        // when
        s3MockProvider.resetToDefaults();

        // then
        ObjectListing objectListing = s3.listObjects(INIT_BUCKET);
        assertThat(objectListing.getObjectSummaries()).extracting("key").containsOnly(INIT_KEY);
    }

    @Test
    void shouldDeleteObject() {
        // when
        s3MockProvider.getS3Mock().delete(INIT_BUCKET, INIT_KEY);

        // then
        ObjectListing objectListing = s3.listObjects(INIT_BUCKET);
        assertThat(objectListing.getObjectSummaries()).isEmpty();
        assertThatThrownBy(() -> s3.getObject(INIT_BUCKET, INIT_KEY)).isInstanceOf(AmazonS3Exception.class);
    }

    private void assertS3Object(S3Object object, String expected) throws IOException {
        assertThat(object).isNotNull();
        try (InputStream s3Is = object.getObjectContent()) {
            String retrieved = IOUtils.toString(s3Is, "UTF-8");
            assertThat(retrieved).isEqualTo(expected);
        }
    }
}
