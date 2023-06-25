package group.rxcloud.ava.aigc.utils;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.ByteBuffer;

@Component
public class AwsFileUtils {

    /**
     * Get ByteBuffer from byte array
     */
    public ByteBuffer getByteBufferFromBytes(byte[] bytes) {
        return ByteBuffer.wrap(bytes);
    }

    /**
     * Get ByteBuffer from InputStream
     */
    public ByteBuffer getByteBufferFromInputStream(InputStream inputStream) {
        try {
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            return getByteBufferFromBytes(bytes);
        } catch (Exception e) {
            throw new UtilsException(e);
        }
    }

    /**
     * Get ByteBuffer from file path
     */
    public ByteBuffer getByteBufferFromFilePath(String filePath) {
        File file = new File(filePath);
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new UtilsException(e);
        }

        return getByteBufferFromInputStream(inputStream);
    }
}
