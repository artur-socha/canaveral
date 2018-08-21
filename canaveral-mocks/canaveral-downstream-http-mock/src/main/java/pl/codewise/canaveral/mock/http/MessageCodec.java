package pl.codewise.canaveral.mock.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;

public class MessageCodec {

    private final ObjectMapper objectMapper;

    public MessageCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public byte[] fileContentToBytes(String pathToResource) {
        try (InputStream resourceAsStream = MessageCodec.class.getResourceAsStream(pathToResource)) {
            return ByteStreams.toByteArray(resourceAsStream);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read file from " + pathToResource, e);
        }
    }
}