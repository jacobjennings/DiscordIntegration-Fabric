package de.erdbeerbaerlp.dcintegration.fabric.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.JsonOps;
import de.erdbeerbaerlp.dcintegration.common.storage.Configuration;
import de.erdbeerbaerlp.dcintegration.common.util.MessageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

public class FabricMessageUtils extends MessageUtils {

    /**
     * Minecraft 26.1 removed the old {@code Text.Serialization} helper that (de)serialized text
     * components to/from JSON. Component (de)serialization now goes through
     * {@link ComponentSerialization#CODEC} with a registry-aware {@link RegistryOps}. These two
     * helpers replace every former {@code Text.Serialization.toJsonString/fromJson} call.
     */
    public static String componentToJson(Component component, HolderLookup.Provider provider) {
        final RegistryOps<JsonElement> ops = provider.createSerializationContext(JsonOps.INSTANCE);
        return ComponentSerialization.CODEC.encodeStart(ops, component).getOrThrow().toString();
    }

    public static MutableComponent componentFromJson(String json, HolderLookup.Provider provider) {
        final RegistryOps<JsonElement> ops = provider.createSerializationContext(JsonOps.INSTANCE);
        return ComponentSerialization.CODEC.parse(ops, JsonParser.parseString(json)).getOrThrow().copy();
    }

    public static String formatPlayerName(ServerPlayer player) {
        if (player.getTabListDisplayName() != null)
            return ChatFormatting.stripFormatting(player.getTabListDisplayName().getString());
        else
            return ChatFormatting.stripFormatting(player.getName().getString());
    }

    public static MessageEmbed genItemStackEmbedIfAvailable(final Component component, Level w) {
        if (!Configuration.instance().forgeSpecific.sendItemInfo) return null;
        JsonObject json;
        try {
            final JsonElement jsonElement = JsonParser.parseString(componentToJson(component, w.registryAccess()));
            if (jsonElement.isJsonObject())
                json = jsonElement.getAsJsonObject();
            else return null;
        } catch (final IllegalStateException ex) {
            ex.printStackTrace();
            return null;
        }
        if (json.has("with")) {
            final JsonArray args = json.getAsJsonArray("with");
            for (JsonElement el : args) {
                if (el instanceof JsonObject arg1) {
                    if (arg1.has("hoverEvent")) {
                        final JsonObject hoverEvent = arg1.getAsJsonObject("hoverEvent");
                        if (hoverEvent.has("action") && hoverEvent.get("action").getAsString().equals("show_item") && hoverEvent.has("contents")) {
                            if (hoverEvent.getAsJsonObject("contents").has("tag")) {
                                final JsonObject item = hoverEvent.getAsJsonObject("contents").getAsJsonObject();
                                try {
                                    final CompoundTag tag = CompoundTagArgument.compoundTag().parse(new StringReader(item.getAsString()));
                                    // 26.1 removed ItemStack.parse/fromNbt; decode via ItemStack.CODEC + registry-aware NbtOps.
                                    final ItemStack is = ItemStack.CODEC.parse(w.registryAccess().createSerializationContext(NbtOps.INSTANCE), tag).result().orElseThrow();

                                    final DataComponentMap itemTag = is.getComponents();
                                    // 26.1: per-component "show in tooltip" flags were replaced by the single
                                    // TOOLTIP_DISPLAY component (TooltipDisplay#shows).
                                    final TooltipDisplay tooltipDisplay = itemTag.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
                                    final EmbedBuilder b = new EmbedBuilder();
                                    Component title = itemTag.getOrDefault(DataComponents.CUSTOM_NAME, Component.translatable(is.getItem().getDescriptionId()));
                                    if (title.getString().isEmpty())
                                        title = Component.translatable(is.getItem().getDescriptionId());
                                    else
                                        b.setFooter(BuiltInRegistries.ITEM.getKey(is.getItem()).toString());
                                    b.setTitle(title.getString());
                                    final StringBuilder tooltip = new StringBuilder();

                                    //Add Enchantments
                                    if (itemTag.has(DataComponents.ENCHANTMENTS)) {
                                        final ItemEnchantments e = itemTag.get(DataComponents.ENCHANTMENTS);
                                        if (e != null && tooltipDisplay.shows(DataComponents.ENCHANTMENTS))
                                            for (Holder<Enchantment> ench : e.keySet()) {
                                                tooltip.append(ChatFormatting.stripFormatting(Enchantment.getFullname(ench, e.getLevel(ench)).getString())).append("\n");
                                            }
                                    }
                                    //Add Lores
                                    if (itemTag.has(DataComponents.LORE)) {
                                        final ItemLore l = itemTag.get(DataComponents.LORE);
                                        if (l != null)
                                            for (Component line : l.lines()) {
                                                tooltip.append("_").append(line.getString()).append("_\n");
                                            }
                                    }
                                    //Add 'Unbreakable' Tag (UNBREAKABLE is now a Unit marker; its tooltip visibility lives in TOOLTIP_DISPLAY)
                                    if (itemTag.has(DataComponents.UNBREAKABLE) && tooltipDisplay.shows(DataComponents.UNBREAKABLE)) {
                                        tooltip.append("Unbreakable\n");
                                    }
                                    b.setDescription(tooltip.toString());
                                    return b.build();
                                } catch (CommandSyntaxException ignored) {
                                    //Just go on and ignore it
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
