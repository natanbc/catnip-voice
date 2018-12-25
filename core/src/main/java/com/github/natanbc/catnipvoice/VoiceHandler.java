package com.github.natanbc.catnipvoice;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface VoiceHandler {
    void handleVoiceServerUpdate(@Nonnull String userId, @Nonnull String guildId, @Nonnull String endpoint,
                                 @Nonnull String token, @Nonnull String sessionId);

    void closeConnection(@Nonnull String userId, @Nonnull String guildId);

    void setAudioProvider(@Nonnull String userId, @Nonnull String guildId, @Nullable AudioProvider audioProvider);

    void shutdown();
}
