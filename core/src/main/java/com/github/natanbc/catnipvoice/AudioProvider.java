package com.github.natanbc.catnipvoice;

import java.nio.ByteBuffer;

public interface AudioProvider {
    boolean canProvide();
    ByteBuffer provide();
    boolean isOpus();
}
