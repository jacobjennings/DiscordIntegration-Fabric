# MCS-6: Retarget DiscordIntegration-Fabric to Minecraft 26.3

Branch: `codex/mcs-6-di-upstream-26-3`, cut from `origin/26.3` at `d83e2944`.
Commits: `391b259` (versions), `d3c1f98` (API drift + fork port). Both pushed to origin.
Started 2026-10-04, 3:21 pm America/Chicago.

## Verdict

The Fabric target builds clean against Minecraft 26.3 and Fabric Loader 0.19.5.
The jar is ready for the merge lane. No known runtime risk that a load test
could not settle quickly.

## Version table

Everything below was checked before use. The old value is what `origin/26.3` carried.

| Property | 26.2 branch | New | Where verified |
|---|---|---|---|
| minecraft_version | 26.2 | 26.3 | meta.fabricmc.net/v2/versions/game, 26.3 is stable |
| fabric_loader_version | 0.19.3 | 0.19.5 | meta.fabricmc.net/v2/versions/loader, newest stable |
| fabric_api_version | 0.157.0+26.2 | 0.161.0+26.3 | maven.fabricmc.net maven-metadata + Modrinth files for `fabric-api` |
| placeholder_api_version | 3.2.0+26.2 | 3.2.0+26.3 | Modrinth API project `placeholder-api` + maven.nucleoid.xyz directory listing |
| styled_chat_version | 2.14.0+26.2 | 2.14.0+26.3 | Modrinth API project `styled-chat` |
| vanish_version | 1.6.16+26.2 | 1.6.16+26.3 | Modrinth API project `vanish`, file `vanish-1.6.16+26.3.jar` |
| fabric_permissions_api_version | 0.7.0 | 0.7.0 (unchanged) | search.maven.org, latest `lp.ez:permissions-api` |
| dcintegration.common | 3.2.1 | 3.2.1 (unchanged, latest) | repo.erdbeerbaerlp.de maven-metadata |
| neoforge channel | 26.2 | 26.3.0.48-beta | maven.neoforged.net releases pom, HTTP 200 |
| Gradle wrapper | 9.5.0 | 9.5.0 (unchanged) | builds and resolves Architectury loom-no-remap 1.17.493 |

The direct Modrinth jar URLs return 404 for every page including old versions,
but the Gradle Modrinth maven resolves them fine. Do not read that 404 as a
missing file.

## Code changes

All in `common/` unless named. Full diff in `git show d3c1f98`.

1. Five `VanillaRegistries.ACCESS` call sites fixed to `server.registryAccess()`.
   Location in ChatBehaviour, PlayerManagerMixin, CommandManagerMixin (five
   uses), ServerInterface and one JSON branch. That accessor is gone in 26.3.
2. `AdvancementMixin`: switched to the 26.3 record accessors
   `announceToChat()`, `title()` and `description()` on
   `AdvancementHolder.Display`.
3. New `CommandSourceStackMixin`. It seeds the `namesProvider` field on
   `CommandSourceStack` so a `DCCommandSender` made by core keeps a usable
   name path in 26.3.
4. `DCCommandSender`: split-name constants `DEFAULT_TEXT_NAME` /
   `DEFAULT_DISPLAY_NAME` and a `defaultNames(Component)` NamesProvider
   factory, passed through the 7-arg `Component` constructor. This keeps the
   Discord sender text name exactly `DiscordIntegration` on 26.3.
5. `ForkChannels.java` ported from `origin/1.20.6` to
   `common/.../architectury/util/`. It reads `DCINTEGRATION_LOGIN_CHANNEL`,
   unset means upstream behavior. Wiring: two `serverChannelID` sites and one
   channel-less JSON join site in `PlayerManagerMixin`, two in
   `NetworkHandlerMixin` (advance and leave), plus join. Contract kept.
6. `fabric.mod.json`: `fabricloader` `>=0.19.5`, `minecraft` `~26.3`. The `~`
   predicate matches the exact minor and will not silently match a patch that
   needs a rebuild.

## Fork change from 1.20.6 that was not ported

`AdvancementMixin` retarget (old fix `401897a`). Not needed. Explanation:

- The old 1.20.6 bug: the mixin anchored in `PlayerAdvancements.tickAll`, so it
  fired repeatedly and spam-announced. The fix anchored on
  `AdvancementRewards.grant`, which runs once at completion.
- The 26.2 upstream multi-loader mixin anchors inside
  `PlayerAdvancements.award(Holder, String)` at the `markForVisibilityUpdate`
  call. 26.3 bytecode for `award` shows that call is guarded: it runs only when
  the player was not already done AND the new progress is done. So it fires
  once per completion. Same contract as the old fix. Porting the anchor would
  change nothing and risk a duplicate announce.

## Mixin audit

Targets verified with `javap` against the 26.3 merged jar
(`~/.gradle/caches/fabric-loom/minecraftMaven/.../26.3/...deobf.jar`).

| Mixin | Target member | 26.3 status |
|---|---|---|
| ChatMixin | ServerGamePacketListenerImpl.handleChat / private 1-arg broadcastChatMessage(PlayerChatMessage) | present, private has exactly one call site, matches |
| CommandManagerMixin | Commands.performCommand(ParseResults, String) | present |
| CommandSourceStackMixin (new) | CommandSourceStack <init> param capture, namesProvider field | present, field type NamesProvider |
| AdvancementMixin | PlayerAdvancements.award(Holder, String), markForVisibilityUpdate(Holder) | present, award guard verified in bytecode |
| MixinMinecraftServer | <init> at RETURN, stopServer | present, handler shape unchanged from 26.2 |
| NetworkHandlerMixin | PlayerList.canPlayerLogin(SocketAddress, NameAndId), placeNewPlayer(Connection, ServerPlayer, CommonListenerCookie), public broadcastChatMessage(PlayerChatMessage, ServerPlayer, ChatType$Bound), ServerGamePacketListenerImpl fields | present |
| ServerPlayerMixin | ServerPlayer.die(DamageSource) | present |
| StyledChatMixin (fabric-like) | StyledChatUtils.modifyForSending / formatMessage | present, checked in styled-chat 2.14.0+26.3 jar |
| disconnect.timeout string check | assets/minecraft/lang/en_us.json in the 26.3 jar | key present, value "Timed out" |

No mixin needs a source-side target change beyond items 1 and 2 above.

## Loaders

`enabled_platforms` stays `fabric,neoforge`. Quilt was already commented out on
the `d83e2944` base, so nothing to drop. NeoForge was kept because `26.3.0.48-beta`
exists on maven.neoforged.net. NeoForge was not compiled here, the Fabric target
owns the build gate for this task.

## Server config comparison (read-only)

Compared the `deploy_minecraft` role (origin/main of ~/gh/mc-server-spinner-upper)
against the core config schema, both the 3.1.0 core the live server uses and the
3.2.1 core this branch will ship.

- Role keys: `botToken`, `botChannel`, `serverChannelID`, `deathsChannelID`,
  `advancementChannelID`, `disableParsingMentionsIngame` in
  `config/Discord-Integration.toml`, plus message keys in
  `DiscordIntegration-Data/Messages.toml`.
- All six keys exist with identical names in both cores. The `advanced` block
  fields also match (serverChannelID, deathsChannelID, advancementChannelID).
- The config file name literal `Discord-Integration.toml` is unchanged in the
  core jar between 3.1.0 and 3.2.1.

Conclusion: no key renamed or moved, the Ansible rewrites will keep working on
the upgraded jar, no role change needed.

## Deliverable

- Jar: `fabric/build/libs/dcintegration-fabric-MC26.3-3.2.1.jar`
- sha256: `0c4c67b34803eb9fca3a08cf2d4732cea71ce7442f39518537186289c32fb7f0`
- Mod id: `dcintegration-fabric`, version `3.2.1`
- `depends`: fabricloader `>=0.19.5`, fabric-api `*`, minecraft `~26.3`

## Checks run

- `./gradlew --no-daemon :fabric:classes` BUILD SUCCESSFUL (4 tasks).
- `./gradlew --no-daemon :fabric:assemble` BUILD SUCCESSFUL (16 tasks).
- All javap member audits above. No server or client was started.

## RECOMMENDATIONS

1. The live loader update path: stlmc should land 0.19.5 in the same restart as
   this jar. `fabric.mod.json` now requires `>=0.19.5`.
2. A smoke test with chat, join, leave and one advancement on an offline 26.3
   server would confirm the `CommandSourceStackMixin` field seed at runtime.
   Nothing about the compile says that inject fires as intended.
3. NeoForge 26.3 is on a beta channel. If the merge gate ever builds it, pin
   the exact `26.3.0.48-beta` and reverify before a stable 26.3 forge release
   lands.
