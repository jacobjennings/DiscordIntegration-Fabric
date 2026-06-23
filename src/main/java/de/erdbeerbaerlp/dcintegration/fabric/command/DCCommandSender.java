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
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;


public class DCCommandSender extends CommandSourceStack {
    private final CompletableFuture<InteractionHook> cmdMsg;
    private CompletableFuture<Message> cmdMessage;
    public final StringBuilder message = new StringBuilder();

    public DCCommandSender(CompletableFuture<InteractionHook> cmdMsg, User user, MinecraftServer server) {
        super(CommandSource.NULL, new Vec3(0, 0, 0), new Vec2(0, 0), server.overworld(), PermissionSet.ALL_PERMISSIONS, user.getAsTag(), Component.literal(user.getAsTag()), server, null);
        this.cmdMsg = cmdMsg;
    }

    public DCCommandSender(MinecraftServer server) {
        super(CommandSource.NULL, new Vec3(0, 0, 0), new Vec2(0, 0), server.overworld(), PermissionSet.ALL_PERMISSIONS, "DiscordIntegration", Component.literal("Discord Integration"), server, null);
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
