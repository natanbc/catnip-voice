package com.github.natanbc.catnipvoice.magma;

import com.github.natanbc.catnipvoice.AudioProvider;
import com.github.natanbc.catnipvoice.VoiceHandler;
import net.dv8tion.jda.core.audio.factory.IAudioSendFactory;
import space.npstr.magma.MagmaApi;
import space.npstr.magma.MagmaMember;
import space.npstr.magma.MagmaServerUpdate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class MagmaHandler implements VoiceHandler {
    private final Map<String, AudioProvider> providerMap = new ConcurrentHashMap<>();
    private final MagmaApi magma;

    public MagmaHandler(Function<String, IAudioSendFactory> sendFactoryFunction) {
        this.magma = MagmaApi.of(m -> sendFactoryFunction.apply(m.getGuildId()));
    }

    public MagmaHandler(IAudioSendFactory sendFactory) {
        this.magma = MagmaApi.of(__ -> sendFactory);
    }

    @Override
    public void handleVoiceServerUpdate(String userId, String guildId, String endpoint, String token, String sessionId) {
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
    public void closeConnection(String userId, String guildId) {
        magma.closeConnection(
                MagmaMember.builder()
                    .userId(userId)
                    .guildId(guildId)
                    .build()
        );
    }

    @Override
    public void setAudioProvider(String userId, String guildId, AudioProvider audioProvider) {
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
}
