package com.haozileung.rpc.common.compression.strategy;

public interface CompressionStrategy {

    CompressionResult compress(byte[] buffer);

    byte[] decompress(byte[] buffer);
}
