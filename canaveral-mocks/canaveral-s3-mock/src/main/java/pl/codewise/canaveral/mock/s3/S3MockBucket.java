package pl.codewise.canaveral.mock.s3;

import org.joda.time.DateTime;

import java.util.Map;

public interface S3MockBucket {

    static S3MockBucket wrap(String bucketName, Map<String, S3MockObject> bucket) {
        return new S3MockBucket() {

            @Override
            public void put(String key, byte[] content) {
                put(key, content, DateTime.now());
            }

            @Override
            public void put(String key, byte[] content, DateTime lastModified) {
                bucket.put(key, S3MockObject.from(key, content, lastModified));
            }

            @Override
            public void delete(String key) {
                bucket.remove(key);
            }

            @Override
            public String name() {
                return bucketName;
            }

            @Override
            public Iterable<S3MockObject> listObjects() {
                return bucket.values();
            }

            @Override
            public S3MockObject get(String key) {
                return bucket.get(key);
            }
        };
    }

    String name();

    S3MockObject get(String key);

    void put(String key, byte[] content);

    void put(String key, byte[] content, DateTime lastModified);

    void delete(String key);

    Iterable<S3MockObject> listObjects();
}
