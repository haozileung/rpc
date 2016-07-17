package com.haozileung.rpc.common.compression.algorithm;

public interface CompressionAlgorithm {

    byte[] compress(byte[] buffer);

    byte[] decompress(byte[] buffer);
}
