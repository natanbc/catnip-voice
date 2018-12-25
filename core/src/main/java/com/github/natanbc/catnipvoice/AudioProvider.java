package com.github.natanbc.catnipvoice;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;

public interface AudioProvider extends AutoCloseable {
    boolean canProvide();

    @Nonnull
    ByteBuffer provide();

    boolean isOpus();

    default void close() {}
}
