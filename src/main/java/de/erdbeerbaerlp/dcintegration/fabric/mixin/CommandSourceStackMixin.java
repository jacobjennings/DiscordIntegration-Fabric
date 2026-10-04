package de.erdbeerbaerlp.dcintegration.fabric.mixin;

import de.erdbeerbaerlp.dcintegration.fabric.command.DCCommandSender;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Minecraft 26.3 derives both the display name and the plain text name of a
 * CommandSourceStack from the single Component passed to its constructor.
 * The default {@link DCCommandSender} needs the pre-26.3 names: display name
 * "Discord Integration" but text name "DiscordIntegration", because the latter is
 * what commands such as ban and ban-ip store as the ban source. This mixin replaces
 * the generated names provider after construction. The namesProvider field is what
 * the withSource, withPermission and similar copy methods carry over, so derived
 * command stacks keep the same distinction.
 */
@Mixin(CommandSourceStack.class)
public abstract class CommandSourceStackMixin {

    @Shadow
    @Final
    @Mutable
    private CommandSourceStack.NamesProvider namesProvider;

    @Inject(
            method = "<init>(Lnet/minecraft/commands/CommandSource;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec2;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/permissions/PermissionSet;Lnet/minecraft/network/chat/Component;Lnet/minecraft/server/MinecraftServer;)V",
            at = @At("TAIL")
    )
    private void dcintegration$restoreSplitNames(CallbackInfo ci) {
        if (!((Object) this instanceof DCCommandSender)) return;
        final Component display = this.namesProvider.displayName(null);
        if (display == DCCommandSender.DEFAULT_DISPLAY_NAME) {
            this.namesProvider = DCCommandSender.defaultNames(display);
        }
    }
}
