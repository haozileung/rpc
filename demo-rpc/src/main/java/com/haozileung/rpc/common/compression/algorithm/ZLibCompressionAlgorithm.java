package com.haozileung.rpc.common.compression.algorithm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class ZLibCompressionAlgorithm implements CompressionAlgorithm {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public byte[] compress(byte[] buffer) {
        byte[] output = new byte[0];
        if (buffer != null) {
            Deflater compressor = new Deflater();
            compressor.reset();
            compressor.setInput(buffer);
            compressor.finish();
            ByteArrayOutputStream bos = new ByteArrayOutputStream(buffer.length);

            byte[] buf = new byte[1024];
            while (!compressor.finished()) {
                int i = compressor.deflate(buf);
                bos.write(buf, 0, i);
            }
            output = bos.toByteArray();
            compressor.end();
        }
        return output;
    }

    public byte[] decompress(byte[] buffer) {
        byte[] output = new byte[0];
        if (buffer != null) {
            Inflater decompressor = new Inflater();
            decompressor.reset();
            decompressor.setInput(buffer);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(buffer.length);
            byte[] buf = new byte[1024];
            while (!decompressor.finished()) {
                int i = 0;
                try {
                    i = decompressor.inflate(buf);
                } catch (DataFormatException e) {
                    logger.error(e.getMessage());
                }
                bos.write(buf, 0, i);
            }
            output = bos.toByteArray();
            decompressor.end();
        }
        return output;
    }
}
