# MCS-1 report: DiscordIntegration fork 3.1.3 build for Minecraft 26.3

Branch: `codex/mcs-1-dcintegration-26-3`. Card: http://huly.lan/workbench/hulyaccessevaluation/tracker/MCS-1

## Result

DiscordIntegration is the chat bridge between Minecraft and Discord, described in the worker guide.
This fork now compiles against Minecraft 26.3 and the gate checks pass. The jar is
`build/libs/dcintegration-fabric-3.1.3-26.3.jar`. The mod version inside is 3.1.3.

## Versions set, and where each was verified

| Property | Old | New | Verified on |
|---|---|---|---|
| `minecraft_version` | 26.1.2 | 26.3 | meta.fabricmc.net `v2/versions/game`, marked stable |
| `loader_version` | 0.19.3 | 0.19.5 | meta.fabricmc.net `v2/versions/loader`, newest stable |
| `loom_version` | 1.16-SNAPSHOT | 1.18.2 | maven.fabricmc.net `net/fabricmc/fabric-loom/`, newest release |
| `fabric_version` | 0.151.0+26.1.2 | 0.161.0+26.3 | Modrinth API, newest file tagged game version 26.3 |
| `placeholder_api_version` | 3.0.0+26.1 | 3.2.0+26.3 | Modrinth API, also listed in maven.nucleoid.xyz |
| `styled_chat_version` | 2.12.0+26.1.2 | 2.14.0+26.3 | Modrinth API, newest 26.3 file |
| `mod_version` | 3.1.2 | 3.1.3 | requested bump |
| `dcintegration_common_version` | 3.1.0 | 3.1.0 (kept) | repo.erdbeerbaerlp.de. Newer 3.2.1 exists, not needed, and behavior stays identical |
| Gradle wrapper | 9.4.0 | 9.8.0 | services.gradle.org `versions/current` |

Why the wrapper had to move: Loom 1.18.x declares the Gradle plugin API at 9.7.0.
Gradle 9.4.0 then refused the plugin variant. So I bumped
`gradle/wrapper/gradle-wrapper.properties` to 9.8.0. That file is outside the brief's file list.
It was the only such change, because every current Loom that supports 26.3 needs a newer Gradle.

`fabric.mod.json` dependency block is now `fabricloader >=0.19.5`, `minecraft ~26.3`, `java >=25`.
I chose `~26.3`. Fabric Loader 0.19.5 compares both the major and the minor part for this
operator, so the predicate accepts 26.3 and its patches, and it rejects 26.4. I kept it because it
covers the requested target. Mojang publishes a machine-readable list
of game versions at piston-meta, which is where versions check their Java needs. That list says 26.3
still needs Java 25, so `>=25` stays.

## Code changes

- `command/DCCommandSender.java`: 26.3 dropped the old 9-argument `CommandSourceStack` constructor. The separate String name and display Component merged into one Component argument. The trailing Entity argument also went. Call 1 keeps `Component.literal(user.getAsTag())`, which preserves both former values. Call 2 now passes `Component.literal("Discord Integration")`. The old internal name string `"DiscordIntegration"` merges into that display name. That is the one small behavior delta I found, and it only shows in `getTextName()` of the Discord command sender.

- `mixin/AdvancementMixin.java`: `DisplayInfo` became a record in 26.x. `shouldAnnounceChat()` becomes `announceToChat()`, `getTitle()` becomes `title()`, `getDescription()` becomes `description()`. The 3.1.2 per-criterion fix stays. It anchors on the `AdvancementRewards.grant(ServerPlayer)` call inside `award`, and I verified both still exist with the same descriptors in the 26.3 jar.

- `mixin/CommandManagerMixin.java` and `mixin/PlayerManagerMixin.java`: `net.minecraft.data.registries.VanillaRegistries` is gone from 26.3. Every `VanillaRegistries.createLookup()` was replaced with a real registry access. CommandManagerMixin uses `source.registryAccess()`. PlayerManagerMixin's login check has no source object, so it goes through `((FabricServerInterface) INSTANCE.getServerInterface()).getServer().registryAccess()`.

- `util/FabricServerInterface.java`: added a `getServer()` getter to make that last line possible.

- No reflection or version-string gating exists in the source, so nothing else could drift silently.

## Mixin-by-mixin runtime audit

Compile checks do not verify mixin targets, so every target was checked with `javap` against
`minecraft-merged-deobf-26.3.jar`. No mixin needed a target change in source.

| Mixin | Target | 26.3 status |
|---|---|---|
| AdvancementMixin | `PlayerAdvancements.award(AdvancementHolder, String)`, inject INVOKE `AdvancementRewards.grant(ServerPlayer)` | both unchanged |
| ChatMixin | `@Redirect` inside `ServerGamePacketListenerImpl.broadcastChatMessage` into `PlayerList.broadcastChatMessage(PlayerChatMessage, ServerPlayer, ChatType$Bound)` | still one matching method and one matching call site. The public 3-arg overload vanished and a private 1-arg holder remains. Name match is unambiguous |
| CommandManagerMixin | `Commands.performCommand(ParseResults, String)` | unchanged |
| MixinMinecraftServer | `MinecraftServer.<init>` (RETURN, argless handler) and `stopServer` | both still exist. The constructor parameter list changed, which does not matter to this injection |
| NetworkHandlerMixin | `ServerGamePacketListenerImpl.onDisconnect(DisconnectionDetails)`, shadow `player` field | unchanged. The `disconnect.timeout` lang key still exists |
| PlayerManagerMixin | `PlayerList.canPlayerLogin(SocketAddress, NameAndId)`, `placeNewPlayer(Connection, ServerPlayer, CommonListenerCookie)` | unchanged |
| ServerPlayerEntityMixin | `ServerPlayer.die(DamageSource)` | unchanged |

## Fork contract

`ForkChannels.java` is untouched. The env var `DCINTEGRATION_LOGIN_CHANNEL` keeps its name, and unset
still falls back to `advanced.serverChannelID` (upstream behavior).

## update-checker.json

The build does not use it. No gradle task reads it. The mod's runtime update checker points at
upstream's raw GitHub copy (`DiscordIntegrationMod.java` line 195), not at this repo file. Left as is.

## Checks run

- `./gradlew classes`: BUILD SUCCESSFUL, 2 actionable tasks: 2 executed. First run failed with 42 error lines (21 unique). That is what drove the code changes above.

- `./gradlew assemble`: BUILD SUCCESSFUL, 4 actionable tasks: 2 executed, 2 up-to-date. Jar produced: `build/libs/dcintegration-fabric-3.1.3-26.3.jar`.

- The repo has no test sources, so no test task was run, and with that, `HULY_BOARD_CONFIG` and `VIKUNJA_BASE_URL` were not needed for anything.

- The jar's `fabric.mod.json` carries version 3.1.3 and the predicate block quoted above.

## README

Checked, not changed. Its only version mention is historical fork framing about the 26.1 move, which
stays true.

## RECOMMENDATIONS

- Out of scope for this card: live styled-chat integration still needs a re-implementation for the new message flow, and it is still recorded in `MixinConfig.java`. The 26.1 port comment is still accurate.

- The loader and Fabric API versions here are the newest 26.3-tagged ones. When stlmc actually restarts on 26.3, one quick `--dry-run` server boot would prove mixin application, since that only fails at runtime.

- Consider pointing the runtime update checker URL at the fork's own branch later. It now checks upstream's repo, so the fork can show upstream releases as updates.
