package com.haozileung.rpc.common.compression.algorithm;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZipCompressionAlgorithm implements CompressionAlgorithm {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private int unit = 2048;

    public byte[] compress(byte[] buffer) {
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        try {
            GZIPOutputStream gzip = new GZIPOutputStream(arrayOutputStream);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer);
            byte[] buf = new byte[unit];
            Integer len = inputStream.read(buf);
            while (len != -1) {
                gzip.write(buf, 0, len);
                len = inputStream.read(buf);
            }
            gzip.finish();
            gzip.close();
            arrayOutputStream.close();
            inputStream.close();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return arrayOutputStream.toByteArray();
    }

    public byte[] decompress(byte[] buffer) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            GZIPInputStream gzip = new GZIPInputStream(inputStream);
            byte[] buf = new byte[unit];
            Integer len = inputStream.read(buf);
            while (len > 0) {
                out.write(buf, 0, len);
                len = gzip.read(buf);
            }
            gzip.close();
            out.close();
            inputStream.close();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return out.toByteArray();
    }

}
