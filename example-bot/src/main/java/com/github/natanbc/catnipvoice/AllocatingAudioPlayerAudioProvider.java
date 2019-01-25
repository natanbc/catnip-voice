package com.github.natanbc.catnipvoice;

import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import com.sedmelluq.discord.lavaplayer.track.playback.ImmutableAudioFrame;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;

public class AllocatingAudioPlayerAudioProvider implements AudioProvider {
    private final ByteBuffer buffer = ByteBuffer.allocate(ExampleBot.LP_FORMAT.maximumChunkSize());
    private final AudioPlayer player;
    private AudioFrame lastFrame;

    public AllocatingAudioPlayerAudioProvider(AudioPlayer player) {
        this.player = player;
    }

    @Override
    public boolean canProvide() {
        return (lastFrame = player.provide()) != null;
    }

    @Nonnull
    @Override
    public ByteBuffer provide() {
        //the correct way would be lastFrame.getData(buffer.array(), 0);
        //but due to a bug in lavaplayer, the offset argument is used as
        //the length of the data to copy

        //instanceof check just to be safe, the bug is only present
        //in ImmutableAudioFrame
        if(lastFrame instanceof ImmutableAudioFrame) {
            lastFrame.getData(buffer.array(), lastFrame.getDataLength());
        } else {
            lastFrame.getData(buffer.array(), 0);
        }
        return buffer.position(0).limit(lastFrame.getDataLength());
    }

    @Override
    public boolean isOpus() {
        return ExampleBot.LP_FORMAT == StandardAudioDataFormats.DISCORD_OPUS;
    }
}
