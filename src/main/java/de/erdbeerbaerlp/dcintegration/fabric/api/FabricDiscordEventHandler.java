package de.erdbeerbaerlp.dcintegration.fabric.api;

import de.erdbeerbaerlp.dcintegration.common.api.DiscordEventHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public abstract class FabricDiscordEventHandler extends DiscordEventHandler {
    public abstract boolean onMcChatMessage(Component txt, ServerPlayer player);
}
