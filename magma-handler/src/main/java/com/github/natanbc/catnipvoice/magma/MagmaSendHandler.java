package com.github.natanbc.catnipvoice.magma;

import com.github.natanbc.catnipvoice.AudioProvider;
import net.dv8tion.jda.core.audio.AudioSendHandler;

public class MagmaSendHandler implements AudioSendHandler {
    private final AudioProvider provider;

    public MagmaSendHandler(AudioProvider provider) {
        this.provider = provider;
    }

    @Override
    public boolean canProvide() {
        return provider.canProvide();
    }

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
