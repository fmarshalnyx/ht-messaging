/// / JacksonJsonSerializer.java
package com.ht.messaging.serialization;

import com.ht.messaging.Serializer;
import org.agrona.concurrent.UnsafeBuffer;

import java.io.IOException;
import java.io.OutputStream;

//import com.fasterxml.jackson.core.JsonFactory;
//import com.fasterxml.jackson.core.JsonParser;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.ht.messaging.Serializer;
//import org.agrona.concurrent.UnsafeBuffer;
//
//import java.io.IOException;
//
public class JacksonJsonSerializer<T> implements Serializer<T> {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final JsonFactory jsonFactory = new JsonFactory(mapper);

    private final Class<T> clazz;
    private final UnsafeBufferOutputStream outputStream;
    private final UnsafeBufferInputStream inputStream;

    public JacksonJsonSerializer(Class<T> clazz) {
        this.clazz = clazz;
        this.outputStream = new UnsafeBufferOutputStream();
        this.inputStream = new UnsafeBufferInputStream();
    }

    @Override
    public void setOutputTarget(UnsafeBuffer buffer, int offset) {
        this.outputStream.reset(buffer, offset);
    }

    @Override
    public int serialize(T obj) {
        try {
            mapper.writeValue(outputStream, obj);
            return outputStream.size();
        } catch (IOException e) {
            throw new RuntimeException("Serialization failed", e);
        }
    }

    @Override
    public void setInputSource(UnsafeBuffer buffer, int offset, int length) {
        this.inputStream.reset(buffer, offset, length);
    }

    @Override
    public T deserialize() {
        try {
            JsonParser parser = jsonFactory.createParser(inputStream);
            return mapper.readValue(parser, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Deserialization failed", e);
        }
    }


    // UnsafeBufferOutputStream.java
    public class UnsafeBufferOutputStream extends OutputStream {

        private UnsafeBuffer buffer;
        private int baseOffset;
        private int position;

        public void reset(UnsafeBuffer buffer, int offset) {
            this.buffer = buffer;
            this.baseOffset = offset;
            this.position = 0;
        }

        @Override
        public void write(int b) throws IOException {
            buffer.putByte(baseOffset + position++, (byte) b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            buffer.putBytes(baseOffset + position, b, off, len);
            position += len;
        }

        public int size() {
            return position;
        }
    }

    // UnsafeBufferInputStream.java
    public class UnsafeBufferInputStream extends InputStream {

        private UnsafeBuffer buffer;
        private int start;
        private int end;
        private int position;

        public void reset(UnsafeBuffer buffer, int offset, int length) {
            this.buffer = buffer;
            this.start = offset;
            this.end = offset + length;
            this.position = offset;
        }

        @Override
        public int read() throws IOException {
            return (position < end) ? buffer.getByte(position++) & 0xFF : -1;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int available = end - position;
            if (available <= 0) return -1;

            int toRead = Math.min(len, available);
            buffer.getBytes(position, b, off, toRead);
            position += toRead;
            return toRead;
        }
    }
}
