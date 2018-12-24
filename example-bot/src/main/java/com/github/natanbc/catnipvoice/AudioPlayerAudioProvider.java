package com.github.natanbc.catnipvoice;

import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;

import java.nio.ByteBuffer;

public class AudioPlayerAudioProvider implements AudioProvider {
    private final ByteBuffer buffer = ByteBuffer.allocate(StandardAudioDataFormats.DISCORD_OPUS.maximumChunkSize());
    private final MutableAudioFrame frame = new MutableAudioFrame();
    private final AudioPlayer player;

    public AudioPlayerAudioProvider(AudioPlayer player) {
        this.player = player;
        frame.setBuffer(buffer);
    }

    @Override
    public boolean canProvide() {
        buffer.clear();
        return player.provide(frame);
    }

    @Override
    public ByteBuffer provide() {
        return buffer.position(0).limit(frame.getDataLength());
    }

    @Override
    public boolean isOpus() {
        return true;
    }
}
