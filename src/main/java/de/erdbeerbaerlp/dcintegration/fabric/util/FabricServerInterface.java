package de.erdbeerbaerlp.dcintegration.fabric.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dcshadow.com.vdurmont.emoji.EmojiParser;
import dcshadow.net.kyori.adventure.text.Component;
import dcshadow.net.kyori.adventure.text.TextReplacementConfig;
import dcshadow.net.kyori.adventure.text.event.ClickEvent;
import dcshadow.net.kyori.adventure.text.event.HoverEvent;
import dcshadow.net.kyori.adventure.text.format.Style;
import dcshadow.net.kyori.adventure.text.format.TextColor;
import dcshadow.net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import dcshadow.net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import de.erdbeerbaerlp.dcintegration.common.DiscordIntegration;
import de.erdbeerbaerlp.dcintegration.common.storage.Configuration;
import de.erdbeerbaerlp.dcintegration.common.storage.Localization;
import de.erdbeerbaerlp.dcintegration.common.storage.linking.LinkManager;
import de.erdbeerbaerlp.dcintegration.common.util.ComponentUtils;
import de.erdbeerbaerlp.dcintegration.common.util.McServerInterface;
import de.erdbeerbaerlp.dcintegration.common.util.MinecraftPermission;
import de.erdbeerbaerlp.dcintegration.fabric.command.DCCommandSender;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.RestAction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class FabricServerInterface implements McServerInterface{
    private final MinecraftServer server;

    public FabricServerInterface(MinecraftServer minecraftServer) {

        this.server = minecraftServer;
    }

    @Override
    public int getMaxPlayers() {
        return server.getPlayerList().getMaxPlayers();
    }

    @Override
    public int getOnlinePlayers() {
        return server.getPlayerCount();
    }

    @Override
    public void sendIngameMessage(Component msg) {
        final List<ServerPlayer> l = server.getPlayerList().getPlayers();
        try {
            for (final ServerPlayer p : l) {
                if (!playerHasPermissions(p, MinecraftPermission.READ_MESSAGES, MinecraftPermission.USER))
                    return;
                if (!DiscordIntegration.INSTANCE.ignoringPlayers.contains(p.getUUID()) && !(LinkManager.isPlayerLinked(p.getUUID()) && LinkManager.getLink(null, p.getUUID()).settings.ignoreDiscordChatIngame)) {
                    final Map.Entry<Boolean, Component> ping = ComponentUtils.parsePing(msg, p.getUUID(), p.getName().getString());
                    final String jsonComp = GsonComponentSerializer.gson().serialize(ping.getValue()).replace("\\\\n", "\n");
                    final MutableComponent comp = FabricMessageUtils.componentFromJson(jsonComp, p.level().registryAccess());
                    p.sendSystemMessage(comp);
                    if (ping.getKey()) {
                        if (LinkManager.isPlayerLinked(p.getUUID())&&LinkManager.getLink(null, p.getUUID()).settings.pingSound) {
                            p.connection.send(new ClientboundSoundPacket(SoundEvents.NOTE_BLOCK_PLING, SoundSource.MASTER, p.position().x, p.position().y, p.position().z, 1, 1, 0L));
                        }
                    }
                }
            }
            //Send to server console too
            final String jsonComp = GsonComponentSerializer.gson().serialize(msg).replace("\\\\n", "\n");
            final MutableComponent comp = FabricMessageUtils.componentFromJson(jsonComp, server.registryAccess());
            server.sendSystemMessage(comp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendIngameReaction(Member member, RestAction<Message> retrieveMessage, UUID targetUUID, EmojiUnion reactionEmote) {
        final List<ServerPlayer> l = server.getPlayerList().getPlayers();
        for (final ServerPlayer p : l) {
            if (!playerHasPermissions(p, MinecraftPermission.READ_MESSAGES, MinecraftPermission.USER))
                return;
            if (p.getUUID().equals(targetUUID) && !DiscordIntegration.INSTANCE.ignoringPlayers.contains(p.getUUID()) && (LinkManager.isPlayerLinked(p.getUUID())&&!LinkManager.getLink(null, p.getUUID()).settings.ignoreDiscordChatIngame && !LinkManager.getLink(null, p.getUUID()).settings.ignoreReactions)) {

                final String emote = reactionEmote.getType() == Emoji.Type.UNICODE ? EmojiParser.parseToAliases(reactionEmote.getName()) : ":" + reactionEmote.getName() + ":";

                Style.Builder memberStyle = Style.style();
                if (Configuration.instance().messages.discordRoleColorIngame)
                    memberStyle = memberStyle.color(TextColor.color(member.getColorRaw()));

                final Component user = Component.text(member.getEffectiveName()).style(memberStyle
                        .clickEvent(ClickEvent.suggestCommand("<@" + member.getId() + ">"))
                        .hoverEvent(HoverEvent.showText(Component.text(Localization.instance().discordUserHover.replace("%user#tag%", member.getUser().getAsTag()).replace("%user%", member.getEffectiveName()).replace("%id%", member.getUser().getId())))));
                final TextReplacementConfig userReplacer = ComponentUtils.replaceLiteral("%user%", user);
                final TextReplacementConfig emoteReplacer = ComponentUtils.replaceLiteral("%emote%", emote);

                final Component out = LegacyComponentSerializer.legacySection().deserialize(Localization.instance().reactionMessage)
                        .replaceText(userReplacer).replaceText(emoteReplacer);

                if (Localization.instance().reactionMessage.contains("%msg%"))
                    retrieveMessage.submit().thenAccept((m) -> {
                        final String msg = FabricMessageUtils.formatEmoteMessage(m.getMentions().getCustomEmojis(), m.getContentDisplay());
                        final TextReplacementConfig msgReplacer = ComponentUtils.replaceLiteral("%msg%", msg);
                        sendReactionMCMessage(p, out.replaceText(msgReplacer));
                    });
                else sendReactionMCMessage(p, out);
            }
        }
    }
    private void sendReactionMCMessage(ServerPlayer target, Component msgComp) {
        final String jsonComp = GsonComponentSerializer.gson().serialize(msgComp).replace("\\\\n", "\n");
        try {
            final MutableComponent comp = FabricMessageUtils.componentFromJson(jsonComp, target.level().registryAccess());
            target.sendSystemMessage(comp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void runMcCommand(String cmd, CompletableFuture<InteractionHook> cmdMsg, User user) {
        final DCCommandSender s = new DCCommandSender(cmdMsg, user, server);
            try {
                server.getCommands().getDispatcher().execute(cmd.trim(), s);
            } catch (CommandSyntaxException e) {
                s.sendFailure(net.minecraft.network.chat.Component.literal(e.getMessage()));
            }
    }

    @Override
    public HashMap<UUID, String> getPlayers() {
        final HashMap<UUID, String> players = new HashMap<>();
        for (final ServerPlayer p : server.getPlayerList().getPlayers()) {
            players.put(p.getUUID(), p.getDisplayName().getString().isEmpty() ? p.getName().getString() : p.getDisplayName().getString());
        }
        return players;
    }

    @Override
    public void sendIngameMessage(String msg, UUID player) {
        final ServerPlayer p = server.getPlayerList().getPlayer(player);
        if (p != null)
            p.sendSystemMessage(net.minecraft.network.chat.Component.literal(msg));
    }

    @Override
    public boolean isOnlineMode() {
        return Configuration.instance().bungee.isBehindBungee || server.usesAuthentication();
    }

    @Override
    public String getNameFromUUID(UUID uuid) {
        return server.services().sessionService().fetchProfile(uuid,false).profile().name();
    }

    @Override
    public String getLoaderName() {
        return "Fabric";
    }

    @Override
    public boolean isPlayerVanish(UUID uuid) {
        // No vanish-mod integration on Fabric; players are never treated as vanished.
        return false;
    }

    @Override
    public boolean playerHasPermissions(UUID player, String... permissions) {
        for (String permission : permissions) {
            for (final MinecraftPermission perm : MinecraftPermission.values()) {
                if(perm.getAsString().equals(permission)){
                    if(Permissions.check(player,perm.getAsString(), perm.getDefaultValue()).join()){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public String runMCCommand(String cmd) {
        final DCCommandSender s = new DCCommandSender(server);
        try {
            server.getCommands().getDispatcher().execute(cmd.trim(), s);
            return s.message.toString();
        } catch (CommandSyntaxException e) {
            return e.getMessage();
        }
    }

    public boolean playerHasPermissions(Player player, String... permissions) {
        for (String permission : permissions) {
            for (MinecraftPermission value : MinecraftPermission.values()) {
                if(value.getAsString().equals(permission)){
                    if(Permissions.check(player,value.getAsString(), value.getDefaultValue())){
                        return true;
                    }
                }
            }
        }
        return false;
    }


    public boolean playerHasPermissions(Player player, MinecraftPermission... permissions) {
        final String[] permissionStrings = new String[permissions.length];
        for (int i = 0; i < permissions.length; i++) {
            permissionStrings[i] = permissions[i].getAsString();
        }
        return playerHasPermissions(player, permissionStrings);
    }
}
