package de.erdbeerbaerlp.dcintegration.architectury.command;

import de.erdbeerbaerlp.dcintegration.architectury.DiscordIntegrationMod;
import de.erdbeerbaerlp.dcintegration.common.DiscordIntegration;
import de.erdbeerbaerlp.dcintegration.common.storage.Localization;
import de.erdbeerbaerlp.dcintegration.common.util.MessageUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.CompletableFuture;

public class DCCommandSender implements CommandSource {
    /**
     * Plain text name used in logs, stats and ban records. Since Minecraft 26.3 the
     * CommandSourceStack Component constructor derives it from the display component,
     * so CommandSourceStackMixin swaps a split-names provider in for the default sender
     * to keep the pre-26.3 text name.
     */
    public static final String DEFAULT_TEXT_NAME = "DiscordIntegration";
    public static final Component DEFAULT_DISPLAY_NAME = Component.literal("Discord Integration");

    private final CompletableFuture<InteractionHook> cmdMsg;
    private final Component name;

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

    private CompletableFuture<Message> cmdMessage;
    public final StringBuilder message = new StringBuilder();

    public DCCommandSender(CompletableFuture<InteractionHook> cmdMsg, User user) {
        final Member member = DiscordIntegration.INSTANCE.getMemberById(user.getId());

        String tag = user.getDiscriminator().equals("0000") ? user.getEffectiveName() : user.getAsTag();
        String hoverText;

        if (member != null) {
            tag = member.getUser().getDiscriminator().equals("0000") ? member.getEffectiveName() : member.getUser().getAsTag();
            hoverText = Localization.instance().discordUserHover
                    .replace("%user#tag%", tag)
                    .replace("%user%", member.getEffectiveName())
                    .replace("%id%", member.getId());
        } else {
            hoverText = Localization.instance().discordUserHover
                    .replace("%user#tag%", tag)
                    .replace("%user%", user.getEffectiveName())
                    .replace("%id%", user.getId());
        }

        this.name = Component.literal("@" + tag).withStyle(style ->
                style.withHoverEvent(new HoverEvent.ShowText(Component.literal(hoverText)))
        );

        this.cmdMsg = cmdMsg;
    }

    public DCCommandSender() {
        this.cmdMsg = null;
        this.name = DEFAULT_DISPLAY_NAME;
    }

    private static String textComponentToDiscordMessage(Component component) {
        if (component == null) return "";
        return MessageUtils.convertMCToMarkdown(component.getString());
    }

    @Override
    public void sendSystemMessage(Component p_215097_) {
        message.append(textComponentToDiscordMessage(p_215097_)).append("\n");
        if (cmdMsg != null) {
            if (cmdMessage == null) {
                cmdMsg.thenAccept((msg) -> {
                    cmdMessage = msg.editOriginal(message.toString().trim()).submit();
                });
            } else {
                cmdMessage.thenAccept((msg) -> {
                    cmdMessage = msg.editMessage(message.toString().trim()).submit();
                });
            }
        }
    }

    @Override
    public boolean acceptsSuccess() {
        return true;
    }

    @Override
    public boolean acceptsFailure() {
        return true;
    }

    public CommandSourceStack createCommandSourceStack() {
        return new CommandSourceStack(
                this,
                Vec3.ZERO,
                Vec2.ZERO,
                DiscordIntegrationMod.server.findRespawnDimension(),
                LevelBasedPermissionSet.OWNER,
                this.name,
                DiscordIntegrationMod.server
        );
    }

    @Override
    public boolean shouldInformAdmins() {
        return true;
    }
}
