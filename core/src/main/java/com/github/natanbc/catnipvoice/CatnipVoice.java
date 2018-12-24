package com.github.natanbc.catnipvoice;

import com.mewna.catnip.entity.user.User;
import com.mewna.catnip.entity.user.VoiceState;
import com.mewna.catnip.extension.AbstractExtension;
import com.mewna.catnip.shard.DiscordEvent;

import java.util.Objects;

public class CatnipVoice extends AbstractExtension {
    private final VoiceHandler voiceHandler;

    public CatnipVoice(VoiceHandler voiceHandler) {
        super("CatnipVoice");
        this.voiceHandler = voiceHandler;
    }

    public void setAudioProvider(String guildId, AudioProvider provider) {
        voiceHandler.setAudioProvider(selfUser().id(), guildId, provider);
    }

    public void closeConnection(String guildId) {
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
            System.out.println("id: " + self.id() + ", guild id: " + vsu.guildId() + ", endpoint " + vsu.endpoint() + ", token " + vsu.token() + ", session id " + vs.sessionId());
            voiceHandler.handleVoiceServerUpdate(self.id(), vsu.guildId(), vsu.endpoint(), vsu.token(), vs.sessionId());
        });
    }

    private User selfUser() {
        var catnip = Objects.requireNonNull(catnip(), "No catnip instance set! Load the extension before using!");
        return Objects.requireNonNull(catnip.selfUser(), "Self user is null! Wait for the catnip instance to be loaded!");
    }
}
