package pl.codewise.canaveral.mock.s3;

import org.eclipse.jetty.server.Server;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.codewise.canaveral.core.runtime.dns.LocalManagedDnsService;
import pl.codewise.canaveral.core.runtime.dns.NameStore;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class S3MockServer implements S3Mock {

    private static final Logger log = LoggerFactory.getLogger(S3MockServer.class);

    private final Server server;
    private final int port;
    private final HashMapS3Storage s3MemoryStorage;

    private S3MockServer(int port) {
        this.s3MemoryStorage = new HashMapS3Storage();
        this.port = port;
        this.server = new Server(port);

        server.setHandler(new S3MockHandler(s3MemoryStorage));
        try {
            start();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot initialize s3 mock.", e);
        }
    }

    static S3Mock start(String host, int port, Set<String> buckets) {
        setupDns(host, buckets);
        return new S3MockServer(port);
    }

    public void start() throws Exception {
        log.debug("Starting S3 Mock.");
        server.start();
    }

    @Override
    public void stop() throws Exception {
        log.debug("Stopping S3 Mock.");
        server.stop();
        server.join();
    }

    @Override
    public int port() {
        return port;
    }

    @Override
    public void clean() {
        log.info("cleaning S3 Mock.");
        s3MemoryStorage.clear();
    }

    @Override
    public S3MockObject get(String bucketName, String key) {
        return s3MemoryStorage.get(bucketName, key);
    }

    @Override
    public void put(String bucket, String key, byte[] content) {
        s3MemoryStorage.put(bucket, key, content);
    }

    @Override
    public void put(String bucket, String key, byte[] content, DateTime lastModified) {
        s3MemoryStorage.put(bucket, key, content, lastModified);
    }

    @Override
    public void delete(String bucket, String key) {
        s3MemoryStorage.delete(bucket, key);
    }

    @Override
    public S3MockBucket getBucket(String bucketName) {
        return S3MockBucket.wrap(bucketName, s3MemoryStorage.listBucket(bucketName));
    }

    @Override
    public Iterable<S3MockBucket> listBuckets() {
        return s3MemoryStorage.listBuckets().stream()
                .map(this::getBucket)
                .collect(Collectors.toList());
    }

    private static void setupDns(String host, Collection<String> buckets) {
        log.debug("Setting up DNS - started");
        NameStore nameStore = NameStore.getInstance();
        for (String bucket : buckets) {
            nameStore.loopback(bucket + "." + host);
        }
        LocalManagedDnsService.installService(false);
        log.debug("Setting up DNS - finished");
    }
}
