package pl.codewise.canaveral.mock.s3;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

class AWSChunkReader implements Iterator<byte[]> {

    private final InputStream stream;
    private final byte[] buffer;
    private State state = State.EXPECT_CHUNK;
    private byte[] next;
    private int dataLength;

    AWSChunkReader(InputStream stream, int bufferSize) {
        this.stream = stream;
        this.buffer = new byte[Math.max(bufferSize, 128)]; // 128 fits chunk-metadata row
        this.next = getNext();
    }

    @Override
    public byte[] next() {
        byte[] current = next;
        next = getNext();

        return current;
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    private byte[] getNext() {
        int read;

        try {
            if (state == State.EXPECT_CHUNK) {
                if ((read = readLine()) > 0) {
                    String data = new String(buffer, 0, read - 2);
                    dataLength = Integer.valueOf(data.split(";")[0], 16);
                    state = State.EXPECT_DATA;
                    return data.getBytes();
                }
                return null;
            } else {
                byte[] result = IOUtils.toByteArray(stream, dataLength);
                read(2);
                state = State.EXPECT_CHUNK;

                return result;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int read(int n) throws IOException {
        int off = 0;
        int count = 0, c;

        while (n > 0 && count < buffer.length && (c = stream.read()) != -1) {
            buffer[off++] = (byte) c;
            count++;
            n--;
        }

        return count > 0 ? count : -1;
    }

    private int readLine() throws IOException {
        int off = 0;
        int count = 0, c;

        while ((c = stream.read()) != -1) {
            buffer[off++] = (byte) c;
            count++;
            if (c == '\n' || count == buffer.length) {
                break;
            }
        }

        return count > 0 ? count : -1;
    }

    private enum State {
        EXPECT_CHUNK,
        EXPECT_DATA
    }
}
