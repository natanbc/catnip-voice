package com.github.natanbc.catnipvoice;

import club.minnced.opus.util.OpusLibrary;
import com.sun.jna.ptr.PointerByReference;
import tomp2p.opuswrapper.Opus;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public class EncodingAudioProvider implements AudioProvider {
    public static final AudioFormat INPUT_FORMAT = new AudioFormat(48000f, 16, 2, true, true);

    public static final int OPUS_SAMPLE_RATE = 48000;
    public static final int OPUS_FRAME_SIZE = 960;
    public static final int OPUS_CHANNEL_COUNT = 2;

    //calculations based on lavaplayer
    //https://github.com/sedmelluq/lavaplayer/blob/facc8ebf3fa0d80896827e07b79303022988acc6/main/src/main/java/com/sedmelluq/discord/lavaplayer/format/Pcm16AudioDataFormat.java
    public static final int MAXIMUM_INPUT_BUFFER_SIZE = 2 /* channels */ * 960 /* samples per chunk */ * 2 /* bytes per sample */;
    //https://github.com/sedmelluq/lavaplayer/blob/facc8ebf3fa0d80896827e07b79303022988acc6/main/src/main/java/com/sedmelluq/discord/lavaplayer/format/OpusAudioDataFormat.java
    //not sure what those magic constants mean
    public static final int MAXIMUM_OUTPUT_BUFFER_SIZE = 32 + 1536 * 960 /* samples per chunk */ / 960;

    private final ShortBuffer intermediateBuffer =
            ByteBuffer.allocateDirect(MAXIMUM_INPUT_BUFFER_SIZE)
                    .order(ByteOrder.nativeOrder())
                    .asShortBuffer();
    private final ByteBuffer resultBuffer = ByteBuffer.allocate(MAXIMUM_OUTPUT_BUFFER_SIZE);
    private final AudioProvider source;
    private final PointerByReference opusEncoder;
    private volatile boolean closed;

    public EncodingAudioProvider(AudioProvider source) {
        //lazy load if needed
        try {
            OpusLibrary.loadFromJar();
        } catch(IOException e) {
            throw new IllegalStateException("Unable to load opus library", e);
        }
        this.source = source;
        var error = IntBuffer.allocate(1);
        this.opusEncoder = Opus.INSTANCE.opus_encoder_create(OPUS_SAMPLE_RATE, OPUS_CHANNEL_COUNT, Opus.OPUS_APPLICATION_AUDIO, error);
        if(error.get() != Opus.OPUS_OK && opusEncoder == null) {
            throw new IllegalStateException("Unable to create opus encoder! Error code: " + error.get(0));
        }
    }

    @Override
    public synchronized boolean canProvide() {
        return source.canProvide();
    }

    @Override
    public synchronized ByteBuffer provide() {
        if(closed) {
            throw new IllegalStateException("Encoder closed");
        }
        var b = source.provide();
        intermediateBuffer.clear().put(b.asShortBuffer()).flip();
        resultBuffer.position(0);
        var amount = Opus.INSTANCE.opus_encode(opusEncoder, intermediateBuffer, OPUS_FRAME_SIZE, resultBuffer, resultBuffer.capacity());
        return resultBuffer.limit(amount);
    }

    @Override
    public boolean isOpus() {
        return true;
    }

    @Override
    public synchronized void close() {
        if(closed) return;
        closed = true;
        Opus.INSTANCE.opus_encoder_destroy(opusEncoder);
        source.close();
    }

    public static AudioProvider wrapIfNeeded(AudioProvider source) {
        if(source.isOpus()) {
            return source;
        }
        return new EncodingAudioProvider(source);
    }
}
