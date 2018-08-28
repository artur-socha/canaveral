package pl.codewise.canaveral.mock.s3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HashMapS3StorageTest {

    private static final String BUCKET_NAME = "aBucket";
    private static final String KEY = "aKey";
    private static final byte[] CONTENT = "content".getBytes();
    private static final String OTHER_BUCKET = "otherBucket";
    private static final String OTHER_KEY = "otherKey";
    private static final byte[] OTHER_CONTENT = "otherContent".getBytes();

    private HashMapS3Storage storage;

    @BeforeEach
    void setUp() {
        storage = new HashMapS3Storage();
    }

    @Test
    void shouldPutAfterGet() {
        // given
        storage.put(BUCKET_NAME, KEY, CONTENT);

        // when
        S3MockObject mockObject = storage.get(BUCKET_NAME, KEY);

        // then
        assertThat(mockObject).isNotNull();
        assertThat(mockObject.content()).isEqualTo(CONTENT);
    }

    @Test
    void shouldDelete() {
        // given
        storage.put(BUCKET_NAME, KEY, CONTENT);

        // when
        storage.delete(BUCKET_NAME, KEY);

        // then
        S3MockObject mockObject = storage.get(BUCKET_NAME, KEY);
        assertThat(mockObject).isNull();
    }

    @Test
    void shouldClear() {
        // given
        String key2 = "key2";

        storage.put(BUCKET_NAME, KEY, CONTENT);
        storage.put(BUCKET_NAME, key2, CONTENT);
        storage.put(OTHER_BUCKET, OTHER_KEY, OTHER_CONTENT);

        // when
        storage.clear();

        // then
        assertThat(storage.get(BUCKET_NAME, KEY)).isNull();
        assertThat(storage.get(BUCKET_NAME, key2)).isNull();
        assertThat(storage.get(OTHER_BUCKET, OTHER_KEY)).isNull();
    }

    @Test
    void shouldListBucket() {
        // given
        storage.put(BUCKET_NAME, KEY, CONTENT);
        storage.put(BUCKET_NAME, OTHER_KEY, OTHER_CONTENT);

        // when
        Map<String, S3MockObject> bucket = storage.listBucket(BUCKET_NAME);

        // then
        assertThat(bucket).isNotNull().hasSize(2);
        assertThat(bucket.get(KEY).content()).isEqualTo(CONTENT);
        assertThat(bucket.get(OTHER_KEY).content()).isEqualTo(OTHER_CONTENT);
    }

    @Test
    void shouldListBuckets() {
        // given
        storage.put(BUCKET_NAME, KEY, CONTENT);
        storage.put(OTHER_BUCKET, OTHER_KEY, OTHER_CONTENT);

        // when
        Collection<String> buckets = storage.listBuckets();

        // then
        assertThat(buckets).isNotNull().containsOnly(BUCKET_NAME, OTHER_BUCKET);
    }

    @Test
    void shouldNotReturnAnyBuckets() {
        // when
        Collection<String> buckets = storage.listBuckets();

        // then
        assertThat(buckets).isNotNull().isEmpty();
    }

    @Test
    void shouldNotReturnAnyObjectsFromNonExistingBucket() {
        // when
        Map<String, S3MockObject> objects = storage.listBucket(BUCKET_NAME);

        // then
        assertThat(objects).isNotNull().isEmpty();
    }

    @Test
    void shouldNotReturnNonExistingObject() {
        // when
        S3MockObject object = storage.get(BUCKET_NAME, KEY);

        // then
        assertThat(object).isNull();
    }

    @Test
    void shouldDeleteNonExistingObject() {
        // when
        storage.delete(BUCKET_NAME, KEY);

        // then
        S3MockObject mockObject = storage.get(BUCKET_NAME, KEY);
        assertThat(mockObject).isNull();
    }
}