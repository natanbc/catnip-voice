package com.github.natanbc.catnipvoice;

import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.entity.user.VoiceState;
import com.mewna.catnip.extension.AbstractExtension;
import com.mewna.catnip.shard.DiscordEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class CatnipVoice extends AbstractExtension {
    private final VoiceHandler voiceHandler;

    public CatnipVoice(@Nonnull VoiceHandler voiceHandler) {
        super("CatnipVoice");
        this.voiceHandler = Objects.requireNonNull(voiceHandler, "Voice handler may not be null");
    }

    public void setAudioProvider(@Nonnull String guildId, @Nullable AudioProvider provider) {
        voiceHandler.setAudioProvider(selfUser().id(), guildId,
                provider == null ? null : EncodingAudioProvider.wrapIfNeeded(provider));
    }

    public void closeConnection(@Nonnull String guildId) {
        voiceHandler.closeConnection(selfUser().id(), guildId);
    }

    @Override
    public void start() {
        on(DiscordEvent.VOICE_SERVER_UPDATE, vsu -> {
            User self = catnip().selfUser();
            //how
            if(self == null) return;
            VoiceState vs = catnip().cache().voiceState(vsu.guildId(), self.id());
            //how²
            if(vs == null) return;
            voiceHandler.handleVoiceServerUpdate(self.id(), vsu.guildId(), vsu.endpoint(), vsu.token(), vs.sessionId());
        });
    }

    @Override
    public void stop() {
        voiceHandler.shutdown();
    }

    @Nonnull
    private User selfUser() {
        var catnip = Objects.requireNonNull(catnip(), "No catnip instance set! Load the extension before using!");
        return Objects.requireNonNull(catnip.selfUser(), "Self user is null! Wait for the catnip instance to be loaded!");
    }
}
