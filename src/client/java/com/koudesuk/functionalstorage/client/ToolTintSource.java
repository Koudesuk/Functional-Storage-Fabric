package com.koudesuk.functionalstorage.client;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.item.ConfigurationToolItem;
import com.koudesuk.functionalstorage.item.LinkingToolItem;
import com.koudesuk.functionalstorage.registry.FSAttachments;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Colors the layers of the Linking and Configuration Tools by their mode, like the original's ToolTintSource
 * ("functionalstorage:tool" in their items/*.json).
 */
public record ToolTintSource(int layer) implements ItemTintSource {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(FunctionalStorage.MOD_ID, "tool");
    public static final MapCodec<ToolTintSource> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("layer").forGetter(ToolTintSource::layer))
            .apply(instance, ToolTintSource::new));

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
        if (stack.getItem() instanceof LinkingToolItem) {
            if (layer != 0 && stack.has(FSAttachments.ENDER_FREQUENCY)) {
                return ARGB.opaque(0x2C9658);
            }
            if (layer == 3 && stack.has(FSAttachments.CONTROLLER)) {
                return ARGB.opaque(0xFF0000);
            }
            if (layer == 1) {
                return ARGB.opaque(LinkingToolItem.getLinkingMode(stack).getColor().getValue());
            }
            if (layer == 2) {
                return ARGB.opaque(LinkingToolItem.getActionMode(stack).getColor().getValue());
            }
        } else if (stack.getItem() instanceof ConfigurationToolItem && layer == 1) {
            return ARGB.opaque(ConfigurationToolItem.getAction(stack).getColor().getValue());
        }
        return -1;
    }

    @Override
    public MapCodec<ToolTintSource> type() {
        return MAP_CODEC;
    }
}
