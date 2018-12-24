# catnip-voice
Send audio from the same process as your [catnip](https://github.com/mewna/catnip) bot

# Usage

1) Add the [jitpack](https://jitpack.io) repository
2) Get a handler implementation (it'll pull the core as a transitive dependency)
   - Currently, the only implementation is based on [magma](https://github.com/napstr/Magma)
   - Group ID: `com.github.natanbc.catnip-voice`, Artifact ID: `magma-handler`, Version: latest commit hash
3) Get an audio send system, such as [jda-nas](https://github.com/sedmelluq/jda-nas)

An example bot is available [here](example-bot/src/main/java/com/github/natanbc/catnipvoice/ExampleBot.java)