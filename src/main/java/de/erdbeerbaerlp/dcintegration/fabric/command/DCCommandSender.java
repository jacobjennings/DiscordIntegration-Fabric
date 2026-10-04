package de.erdbeerbaerlp.dcintegration.fabric.command;

import de.erdbeerbaerlp.dcintegration.common.util.MessageUtils;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;


public class DCCommandSender extends CommandSourceStack {
    /**
     * Plain text name used in logs, stats and ban records. Since Minecraft 26.3 the
     * {@link CommandSourceStack} Component constructor derives it from the display component,
     * so {@link de.erdbeerbaerlp.dcintegration.fabric.mixin.CommandSourceStackMixin} swaps a
     * split-names provider in for the default sender to keep the pre-26.3 text name.
     */
    public static final String DEFAULT_TEXT_NAME = "DiscordIntegration";
    public static final Component DEFAULT_DISPLAY_NAME = Component.literal("Discord Integration");

    private final CompletableFuture<InteractionHook> cmdMsg;
    private CompletableFuture<Message> cmdMessage;
    public final StringBuilder message = new StringBuilder();

    public static CommandSourceStack.NamesProvider defaultNames(Component display) {
        return new CommandSourceStack.NamesProvider() {
            @Override
            public Component displayName(Entity entity) {
                return display;
            }

            @Override
            public String textName(Entity entity) {
                return DEFAULT_TEXT_NAME;
            }
        };
    }

    public DCCommandSender(CompletableFuture<InteractionHook> cmdMsg, User user, MinecraftServer server) {
        super(CommandSource.NULL, new Vec3(0, 0, 0), new Vec2(0, 0), server.overworld(), PermissionSet.ALL_PERMISSIONS, Component.literal(user.getAsTag()), server);
        this.cmdMsg = cmdMsg;
    }

    public DCCommandSender(MinecraftServer server) {
        super(CommandSource.NULL, new Vec3(0, 0, 0), new Vec2(0, 0), server.overworld(), PermissionSet.ALL_PERMISSIONS, DEFAULT_DISPLAY_NAME, server);
        this.cmdMsg = null;
    }


    private static String textComponentToDiscordMessage(Component component) {
        if (component == null) return "";
        return MessageUtils.convertMCToMarkdown(component.getString());
    }

    @Override
    public void sendSuccess(Supplier<Component> feedbackSupplier, boolean broadcastToOps) {
        message.append(textComponentToDiscordMessage(feedbackSupplier.get())).append("\n");
        if (cmdMsg != null)
            if (cmdMessage == null)
                cmdMsg.thenAccept((msg) -> {
                    cmdMessage = msg.editOriginal(message.toString().trim()).submit();
                });
            else
                cmdMessage.thenAccept((msg) -> {
                    cmdMessage = msg.editMessage(message.toString().trim()).submit();
                });
    }

    @Override
    public void sendFailure(Component message) {
        this.sendSuccess(() -> message, false);
    }
}
