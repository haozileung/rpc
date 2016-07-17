package com.haozileung.rpc.common.compression.strategy;

public class CompressionResult {
    private boolean isCompressed;
    private byte[] buffer;

    public CompressionResult(boolean isCompressed, byte[] buffer) {
        this.isCompressed = isCompressed;
        this.buffer = buffer;
    }

    public boolean isCompressed() {
        return isCompressed;
    }

    public void setCompressed(boolean compressed) {
        isCompressed = compressed;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }
}
