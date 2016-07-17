package com.haozileung.rpc.common.compression.strategy;


import com.haozileung.rpc.common.compression.algorithm.CompressionAlgorithm;
import com.haozileung.rpc.common.compression.algorithm.ZLibCompressionAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThresholdCompressionStrategy implements CompressionStrategy {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private Integer threshold = 10240;
    private CompressionAlgorithm compressionAlgorithm = new ZLibCompressionAlgorithm();

    public Integer getThreshold() {
        return threshold;
    }

    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
    }

    public CompressionAlgorithm getCompressionAlgorithm() {
        return compressionAlgorithm;
    }

    public void setCompressionAlgorithm(CompressionAlgorithm compressionAlgorithm) {
        this.compressionAlgorithm = compressionAlgorithm;
    }

    @Override
    public CompressionResult compress(byte[] buffer) {
        CompressionResult result = new CompressionResult(false, buffer);
        if (buffer != null && buffer.length > threshold) {
            byte[] bytes;
            bytes = compressionAlgorithm.compress(buffer);
            if (bytes.length < buffer.length) {
                result.setBuffer(bytes);
                result.setCompressed(true);
            }
        }
        return result;
    }

    @Override
    public byte[] decompress(byte[] buffer) {
        return compressionAlgorithm.decompress(buffer);
    }
}
