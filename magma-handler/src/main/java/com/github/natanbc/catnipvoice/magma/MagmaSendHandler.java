package com.github.natanbc.catnipvoice.magma;

import com.github.natanbc.catnipvoice.AudioProvider;
import net.dv8tion.jda.core.audio.AudioSendHandler;

import javax.annotation.Nonnull;
import java.util.Objects;

public class MagmaSendHandler implements AudioSendHandler {
    private final AudioProvider provider;

    public MagmaSendHandler(@Nonnull AudioProvider provider) {
        this.provider = Objects.requireNonNull(provider, "Provider may not be null");
    }

    @Override
    public boolean canProvide() {
        return provider.canProvide();
    }

    @Nonnull
    @Override
    public byte[] provide20MsAudio() {
        var buffer = provider.provide();
        var pos = buffer.position();
        var array = new byte[buffer.remaining()];
        buffer.get(array);
        buffer.position(pos);
        return array;
    }

    @Override
    public boolean isOpus() {
        return provider.isOpus();
    }
}
