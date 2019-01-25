package com.github.natanbc.catnipvoice;

import com.github.natanbc.catnipvoice.magma.MagmaHandler;
import com.mewna.catnip.Catnip;
import com.mewna.catnip.shard.DiscordEvent;
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class ExampleBot {
    //change to DISCORD_PCM_S16_BE to test the built in opus encoding
    public static final AudioDataFormat LP_FORMAT = StandardAudioDataFormats.DISCORD_OPUS;
    public static final boolean USE_NON_ALLOCATING_FRAME_BUFFER = false;

    public static void main(String[] args) throws IOException {
        var sendFactory = new NativeAudioSendFactory();
        var handler = new MagmaHandler(sendFactory);

        var playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);

        playerManager.getConfiguration().setOutputFormat(LP_FORMAT);

        if(USE_NON_ALLOCATING_FRAME_BUFFER) {
            playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
        }

        var catnip = Catnip.catnip(Files.readString(Path.of("token.txt")));

        catnip.loadExtension(new CatnipVoice(handler));

        catnip.on(DiscordEvent.MESSAGE_CREATE, message -> {
            if(message.content().startsWith("!play")) {
                var guild = message.guildId();
                if(guild == null) {
                    message.channel().sendMessage("This command cannot be used in DMs!");
                    return;
                }
                var what = message.content().substring(5).strip();
                if(what.isEmpty()) {
                    message.channel().sendMessage("You need to specify what to play!");
                    return;
                }
                var voiceState = catnip.cache().voiceState(guild, message.author().id());
                if(voiceState == null || voiceState.channelId() == null) {
                    message.channel().sendMessage("You need to join a voice channel!");
                    return;
                }
                var player = playerManager.createPlayer();
                var extension = Objects.requireNonNull(catnip.extensionManager().extension(CatnipVoice.class));

                catnip.openVoiceConnection(guild, Objects.requireNonNull(voiceState.channelId()));

                if(USE_NON_ALLOCATING_FRAME_BUFFER) {
                    extension.setAudioProvider(guild, new NonAllocatingAudioPlayerAudioProvider(player));
                } else {
                    extension.setAudioProvider(guild, new AllocatingAudioPlayerAudioProvider(player));
                }

                playerManager.loadItem(what, new AudioLoadResultHandler() {
                    @Override
                    public void trackLoaded(AudioTrack track) {
                        message.channel().sendMessage("Starting track");
                        player.playTrack(track);
                    }

                    @Override
                    public void playlistLoaded(AudioPlaylist playlist) {
                        message.channel().sendMessage("Starting track");
                        player.playTrack(playlist.getTracks().get(0));
                    }

                    @Override
                    public void noMatches() {
                        message.channel().sendMessage("No matches!");
                    }

                    @Override
                    public void loadFailed(FriendlyException exception) {
                        message.channel().sendMessage("Load failed!");
                        exception.printStackTrace();
                    }
                });
            }
        });

        catnip.connect();
    }
}
