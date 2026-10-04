# MCS-1 fix-up: Discord sender text name and Gradle 9.8.0 wrapper

Run branch: `codex/mcs-1-fix-textname-wrapper`, cut from `origin/codex/mcs-1-dcintegration-26-3` at `be7b969`.
Board cards: [MCS-1](http://huly.lan/workbench/hulyaccessevaluation/tracker/MCS-1), review on [MCS-15](http://huly.lan/workbench/hulyaccessevaluation/tracker/MCS-15).
Run started 1:26 am, finished about 2:35 am America/Chicago.
`DiscordIntegration` there means the mod's plain text name for its own command sender. Minecraft
stores that string as the author of a ban.

## Changes

### 1. Sender text name restored (blocking review finding)

`command/DCCommandSender.java` now holds the two names it needs as constants: display name
`Discord Integration` and text name `DiscordIntegration`. The default sender keeps passing the
display component to the 26.3 `CommandSourceStack` Component constructor, so the display name is
unchanged. A new mixin `mixin/CommandSourceStackMixin.java` injects at the TAIL of that
constructor. When the instance being built is a `DCCommandSender` and the component passed is the
shared `DEFAULT_DISPLAY_NAME` instance, the mixin replaces the private `namesProvider` field with
a split-names provider. Its `displayName` returns the same component and its `textName` returns
`DiscordIntegration`. Registered by adding `CommandSourceStackMixin` to the server list in
`src/main/resources/dcintegration-fabric.mixins.json`.

Derived stacks keep the distinction. In 26.3 every `with*` copy method passes the existing
`namesProvider` field object straight into the private full constructor. So the split provider rides
along on every stack derived from the sender. Command stacks derived by `withSource`,
`withPermission` or the other copy methods therefore still write `DiscordIntegration` into ban
records.

The user-based sender is untouched and matches its 1.20.6 behavior. There the display component
was built from `user.getAsTag()`, and the derived text name equals that same tag string.

### 2. javap evidence against the 26.3 jar

Inspected `net.minecraft.commands.CommandSourceStack` in
`~/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged-deobf/26.3/minecraft-merged-deobf-26.3.jar`
with `javap -p` and `javap -c`.

- Public constructors take a `Component` or an `MinecraftServer` and `Entity`. Neither takes a
  separate text name. Bytecode of the Component one shows it calls the private constructor with
  `constant(component)`.

- `NamesProvider` is a public nested interface with `displayName(Entity)` and `textName(Entity)`.
  Its `constant` implementation returns the component for the display name, and the component's
  string for the text name. That is why the 26.3 port lost the plain text name.

- A private constructor takes a provider directly, and the two name getters only query that
  field. The field is private final, so no compile-time call site in a normal subclass can set it.
  That is why the mixin rewrites the field. A getter override would not survive the `with*` copies.

- `withSource` reads its fields including the provider and calls the private full constructor on
  a plain `CommandSourceStack`. That proves the provider object, not a method override, is what
  survives copies.

### 3. Report correction

`docs/harness/reports/mcs-1-dcintegration-26-3/report.md` said `~26.3` covers every 26.3.x and
later 26.x. That was wrong. `Fabric Loader` is the program that loads this mod on the server, and
0.19.5 compares both the major and the minor part for that operator. So it accepts 26.3 and its
patches and rejects 26.4. The implemented predicate stays.

### 4. Gradle wrapper regenerated

The commit that bumped Gradle edited only `gradle-wrapper.properties`, so `gradle/wrapper/gradle-wrapper.jar`
was still the 9.4.0 jar (sha256 `55243ef57851f12b070ad14f7f5bb8302daceeebc5bce5ece5fa6edb23e1145c`).
Ran `./gradlew wrapper --gradle-version 9.8.0 --gradle-distribution-sha256-sum bafd5ce9cfaea0fbccfdc8439a1ac42fbd4cd9c89dc9a988228d8a2639a58e6c`.
Before committing, the regenerated jar printed exactly:

```text
238e777fcddd7e34f9708186085def2abd6e08e658505b38718d79d74c21abd5  gradle/wrapper/gradle-wrapper.jar
```

`gradle/wrapper/gradle-wrapper.properties` keeps
`distributionUrl=https\://services.gradle.org/distributions/gradle-9.8.0-bin.zip` and now also pins
`distributionSha256Sum=bafd5ce9cfaea0fbccfdc8439a1ac42fbd4cd9c89dc9a988228d8a2639a58e6c`, which is the
official 9.8.0 bin distribution checksum. Committed all four regenerated files: the jar, the
properties file, `gradlew` and `gradlew.bat`.

## Checks

Ran from this worktree after `./gradlew clean`.

- `./gradlew classes`: BUILD SUCCESSFUL, 2 actionable tasks: 2 executed.
- `./gradlew assemble`: BUILD SUCCESSFUL, 4 actionable tasks: 2 executed, 2 up-to-date.
  Jar output: `build/libs/dcintegration-fabric-3.1.3-26.3.jar`.

The jar was not installed or deployed. No server, client or Discord connection was touched. No lab
host was contacted. The repo has no test sources, so no tests ran.

## Files changed outside the brief's list

- `src/main/resources/dcintegration-fabric.mixins.json`: one line added to register
  `CommandSourceStackMixin`. Fabric only applies mixins that its config lists, and the task was not
  doable through public 26.3 API alone, so the registration was required.
- `src/main/java/de/erdbeerbaerlp/dcintegration/fabric/mixin/CommandSourceStackMixin.java` sits
  under the `src/main/java/de/erdbeerbaerlp/dcintegration/fabric/` directory entry in the brief.

## RECOMMENDATIONS

- The merge lane should run one live-server smoke test at its first opportunity with a `ban` run
  through a Discord command, and read `banned players.json`, to confirm the text name at runtime.
  Runtime mixin application was not observable from build-only checks, and this run may not start a server.
- Consider upstreaming attention to the fact that Mojang gives mods no public seam for split
  command-source names. A later mapping or an official accessor could retire the mixin.
