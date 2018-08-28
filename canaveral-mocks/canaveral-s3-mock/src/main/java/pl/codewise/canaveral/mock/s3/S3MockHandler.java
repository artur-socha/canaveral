package pl.codewise.canaveral.mock.s3;

import com.google.common.base.Throwables;
import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class S3MockHandler extends InMemoryS3Handler {

    private static final Logger LOG = LoggerFactory.getLogger(S3MockHandler.class);

    S3MockHandler(HashMapS3Storage s3MemoryStorage) {
        super(s3MemoryStorage);
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String key = URLDecoder.decode(request.getRequestURI().substring(1), "UTF-8");

        if (key.equals("closeS3Mock") && request.getMethod().equals("POST")) {
            LOG.info("Received request to stop s3Mock from: {}", request.getRemoteUser());
            baseRequest.setHandled(true);
            Executors.newSingleThreadScheduledExecutor().schedule(this::stopServerSilently, 100, TimeUnit.MILLISECONDS);
            return;
        }
        super.handle(target, baseRequest, request, response);
    }

    private void stopServerSilently() {
        try {
            this.getServer().stop();
            LOG.info("Server stopped successfully");
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
