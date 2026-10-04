# build-toolchain

How to build this Fabric mod locally (userspace JDK 25 + Gradle, no sudo, never launch MC)

This repo (DiscordIntegration-Fabric, 26.1.2 branch) builds with a **userspace** toolchain installed under `~/.local/di-toolchain/` because the machine has no system JDK and `sudo`/pacman needs a password:
- JDK 25: `~/.local/di-toolchain/jdk25` (Temurin 25.0.3 LTS)
- Gradle 9.4.0: `~/.local/di-toolchain/gradle-9.4.0` (also committed as `./gradlew`)

To build: `JAVA_HOME=~/.local/di-toolchain/jdk25 ./gradlew --no-daemon build`. The repo now has a committed wrapper.

**Never run `runClient`/`runServer`** — the user trains an AI model on the GPU and does not want Minecraft consuming VRAM. Verify changes by **compiling only** (`build`/`compileJava`). Note a `gradle build` does NOT validate mixin `@At`/`method` targets (that happens at game launch); verify those against the decompiled jar with `javap -p -c` on `~/.gradle/caches/fabric-loom/26.1.2/minecraft-merged.jar`. See no-agents-trap.
