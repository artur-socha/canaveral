package pl.codewise.canaveral.mock.s3;

import org.joda.time.DateTime;

public interface S3Mock {

    S3MockObject get(String bucketName, String key);

    void put(String bucket, String key, byte[] content);

    void put(String bucket, String key, byte[] content, DateTime lastModified);

    void delete(String bucket, String key);

    S3MockBucket getBucket(String bucketName);

    Iterable<S3MockBucket> listBuckets();

    void start() throws Exception;

    void stop() throws Exception;

    int port();

    void clean();
}
