# kash-helper-jda example

Minimal example submodule for JDA platform integration.

The example uses a resource-backed XML locale: `example-jda-locale.xml`.

- Embed paths use component resolution (`messageId.componentId`) such as `guild-info.container`.
- Use `/ping` for the basic reply.
- Use `/guild-info` to post or refresh a persistent embed in the current channel.
- Use `/guild-info-dm` to send a persistent DM embed; if DMs are disabled, a fallback reply message is used (`direct-message-not-allowed`).

Run locally:

```bash
./gradlew :kash-helper-jda:example:runExample
```
