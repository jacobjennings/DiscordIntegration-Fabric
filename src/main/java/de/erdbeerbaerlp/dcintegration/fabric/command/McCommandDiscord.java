package de.erdbeerbaerlp.dcintegration.fabric.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.erdbeerbaerlp.dcintegration.common.DiscordIntegration;
import de.erdbeerbaerlp.dcintegration.common.minecraftCommands.MCSubCommand;
import de.erdbeerbaerlp.dcintegration.common.minecraftCommands.McCommandRegistry;
import de.erdbeerbaerlp.dcintegration.common.storage.Configuration;
import de.erdbeerbaerlp.dcintegration.common.util.MinecraftPermission;
import de.erdbeerbaerlp.dcintegration.fabric.util.FabricServerInterface;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;

import java.net.URI;

public class McCommandDiscord {
    public McCommandDiscord(CommandDispatcher<CommandSourceStack> dispatcher) {
        final LiteralArgumentBuilder<CommandSourceStack> l = Commands.literal("discord");
        if (Configuration.instance().ingameCommand.enabled) l.executes((ctx) -> {
            ctx.getSource().sendSuccess(() -> ComponentUtils.mergeStyles(Component.literal(Configuration.instance().ingameCommand.message),
                    Style.EMPTY.withHoverEvent(new HoverEvent.ShowText(Component.literal(Configuration.instance().ingameCommand.hoverMessage)))
                            .withClickEvent(new ClickEvent.OpenUrl(URI.create(Configuration.instance().ingameCommand.inviteURL)))), false);
            return 0;
        }).requires((s) -> {
            try {
                return ((FabricServerInterface) DiscordIntegration.INSTANCE.getServerInterface()).playerHasPermissions(s.getPlayerOrException(), MinecraftPermission.USER, MinecraftPermission.RUN_DISCORD_COMMAND);
            }catch (CommandSyntaxException e) {
                return true;
            }
        });
        for (final MCSubCommand cmd : McCommandRegistry.getCommands()) {
            l.then(Commands.literal(cmd.getName()));
        }
        dispatcher.register(l);
    }
}
