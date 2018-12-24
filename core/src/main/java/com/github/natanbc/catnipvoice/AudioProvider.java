package com.github.natanbc.catnipvoice;

import java.nio.ByteBuffer;

public interface AudioProvider extends AutoCloseable {
    boolean canProvide();

    ByteBuffer provide();

    boolean isOpus();

    default void close() {}
}
