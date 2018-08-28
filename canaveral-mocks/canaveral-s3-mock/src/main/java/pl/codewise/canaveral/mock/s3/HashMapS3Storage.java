package pl.codewise.canaveral.mock.s3;

import org.joda.time.DateTime;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

class HashMapS3Storage {

    private final Map<String, ConcurrentHashMap<String, S3MockObject>> storage = new HashMap<>();

    synchronized Collection<String> listBuckets() {
        return storage.keySet().stream().sorted().collect(Collectors.toList());
    }

    synchronized Map<String, S3MockObject> listBucket(String bucketName) {
        return getBucket(bucketName);
    }

    synchronized S3MockObject get(String bucketName, String key) {
        return getBucket(bucketName).get(key);
    }

    synchronized void put(String bucketName, String key, byte[] content) {
        put(bucketName, key, content, DateTime.now());
    }

    synchronized void put(String bucketName, String key, byte[] content, DateTime lastModified) {
        getBucket(bucketName).put(key, S3MockObject.from(key, content, lastModified));
    }

    synchronized void delete(String bucketName, String key) {
        getBucket(bucketName).remove(key);
    }

    synchronized void clear() {
        storage.values().forEach(Map::clear);
    }

    private  synchronized Map<String, S3MockObject> getBucket(String bucketName) {
        return storage.computeIfAbsent(bucketName, k -> new ConcurrentHashMap<>());
    }
}
