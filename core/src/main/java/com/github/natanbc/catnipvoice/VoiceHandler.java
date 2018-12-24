package com.github.natanbc.catnipvoice;

public interface VoiceHandler {
    void handleVoiceServerUpdate(String userId, String guildId, String endpoint, String token, String sessionId);

    void closeConnection(String userId, String guildId);

    void setAudioProvider(String userId, String guildId, AudioProvider audioProvider);
}
