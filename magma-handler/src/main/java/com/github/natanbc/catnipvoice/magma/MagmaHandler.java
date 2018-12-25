package com.github.natanbc.catnipvoice.magma;

import com.github.natanbc.catnipvoice.AudioProvider;
import com.github.natanbc.catnipvoice.EncodingAudioProvider;
import com.github.natanbc.catnipvoice.VoiceHandler;
import net.dv8tion.jda.core.audio.factory.IAudioSendFactory;
import space.npstr.magma.MagmaApi;
import space.npstr.magma.MagmaMember;
import space.npstr.magma.MagmaServerUpdate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class MagmaHandler implements VoiceHandler {
    private final Map<String, AudioProvider> providerMap = new ConcurrentHashMap<>();
    private final MagmaApi magma;

    public MagmaHandler(@Nonnull Function<String, IAudioSendFactory> sendFactoryFunction) {
        this.magma = MagmaApi.of(m -> sendFactoryFunction.apply(m.getGuildId()));
    }

    public MagmaHandler(@Nonnull IAudioSendFactory sendFactory) {
        this.magma = MagmaApi.of(__ -> sendFactory);
    }

    @Override
    public void handleVoiceServerUpdate(@Nonnull String userId, @Nonnull String guildId, @Nonnull String endpoint,
                                        @Nonnull String token, @Nonnull String sessionId) {
        magma.provideVoiceServerUpdate(
                MagmaMember.builder()
                    .userId(userId)
                    .guildId(guildId)
                    .build(),
                MagmaServerUpdate.builder()
                    .endpoint(endpoint)
                    .token(token)
                    .sessionId(sessionId)
                    .build()
        );
    }

    @Override
    public void closeConnection(@Nonnull String userId, @Nonnull String guildId) {
        var old = providerMap.remove(guildId);
        if(old != null) {
            old.close();
        }
        var member = MagmaMember.builder()
                .userId(userId)
                .guildId(guildId)
                .build();
        magma.closeConnection(member);
        magma.removeSendHandler(member);
    }

    @Override
    public void setAudioProvider(@Nonnull String userId, @Nonnull String guildId, @Nullable AudioProvider audioProvider) {
        if(audioProvider != null && !audioProvider.isOpus()) {
            audioProvider = new EncodingAudioProvider(audioProvider);
        }
        var member = MagmaMember.builder()
                .userId(userId)
                .guildId(guildId)
                .build();
        var old = providerMap.put(guildId, audioProvider);
        if(old != null) {
            old.close();
        }
        if(audioProvider == null) {
            magma.removeSendHandler(member);
        } else {
            magma.setSendHandler(member, new MagmaSendHandler(audioProvider));
        }
    }

    @Override
    public void shutdown() {
        magma.shutdown();
    }
}
