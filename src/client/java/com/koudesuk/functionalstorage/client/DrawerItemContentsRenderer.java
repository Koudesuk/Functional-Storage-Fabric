package com.koudesuk.functionalstorage.client;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.koudesuk.functionalstorage.block.tile.CompactingDrawerTile;
import com.koudesuk.functionalstorage.block.tile.ControllableDrawerTile;
import com.koudesuk.functionalstorage.block.tile.DrawerTile;
import com.koudesuk.functionalstorage.block.tile.FluidDrawerTile;
import com.koudesuk.functionalstorage.block.tile.SimpleCompactingDrawerTile;
import com.koudesuk.functionalstorage.item.DrawerBlockItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * The contents drawn on a drawer item, like the original's DrawerISTER, CompactingDrawerISTER and
 * FluidDrawerISTER. Called by ItemRendererMixin once the item model is drawn, in the model's block space.
 */
public final class DrawerItemContentsRenderer {

    private static final DrawerRenderer<DrawerTile> DRAWER = new DrawerRenderer<>();
    private static final CompactingDrawerRenderer<CompactingDrawerTile> COMPACTING = new CompactingDrawerRenderer<>();
    private static final SimpleCompactingDrawerRenderer<SimpleCompactingDrawerTile> SIMPLE_COMPACTING =
            new SimpleCompactingDrawerRenderer<>();
    private static final FluidDrawerRenderer FLUID = new FluidDrawerRenderer();

    // Loading a drawer from its item every frame is costly; the copied tag tells when the item changed
    private static final Cache<ItemStack, Loaded> TILES = CacheBuilder.newBuilder().weakKeys().maximumSize(512).build();
    private static boolean rendering;

    private record Loaded(CompoundTag tag, @Nullable ControllableDrawerTile<?> tile) {
    }

    private DrawerItemContentsRenderer() {
    }

    public static void render(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        // A drawer item stored in a drawer is drawn without its own contents
        if (rendering || !(stack.getItem() instanceof DrawerBlockItem item))
            return;
        ControllableDrawerTile<?> tile = load(stack, item);
        if (tile == null)
            return;
        rendering = true;
        try {
            if (tile instanceof FluidDrawerTile fluid) {
                FLUID.renderItemContents(fluid, poseStack, buffer, light, overlay);
            } else if (tile instanceof DrawerTile drawer) {
                DRAWER.renderItemContents(drawer, poseStack, buffer, light, overlay);
            } else if (tile instanceof CompactingDrawerTile compacting) {
                COMPACTING.renderItemContents(compacting, poseStack, buffer, light, overlay);
            } else if (tile instanceof SimpleCompactingDrawerTile simple) {
                SIMPLE_COMPACTING.renderItemContents(simple, poseStack, buffer, light, overlay);
            }
        } finally {
            rendering = false;
        }
    }

    @Nullable
    private static ControllableDrawerTile<?> load(ItemStack stack, DrawerBlockItem item) {
        CompoundTag tag = DrawerBlockItem.tileTag(stack);
        if (tag == null)
            return null;
        Loaded loaded = TILES.getIfPresent(stack);
        if (loaded == null || !loaded.tag().equals(tag)) {
            loaded = new Loaded(tag.copy(), item.loadTile(stack));
            TILES.put(stack, loaded);
        }
        return loaded.tile();
    }
}
