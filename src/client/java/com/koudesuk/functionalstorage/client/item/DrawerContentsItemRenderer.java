package com.koudesuk.functionalstorage.client.item;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.block.tile.CompactingDrawerTile;
import com.koudesuk.functionalstorage.block.tile.ControllableDrawerTile;
import com.koudesuk.functionalstorage.block.tile.DrawerTile;
import com.koudesuk.functionalstorage.block.tile.FluidDrawerTile;
import com.koudesuk.functionalstorage.block.tile.SimpleCompactingDrawerTile;
import com.koudesuk.functionalstorage.client.DrawerContentsRenderer;
import com.koudesuk.functionalstorage.item.DrawerBlockItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * Draws what a drawer item holds on its front, a port of the original's DrawerSpecialRenderer
 * ("functionalstorage:drawer" in the drawers' items/*.json). The item's drawer is loaded into an unplaced tile
 * and drawn by the same code as the placed drawer, facing north like the item model.
 */
public final class DrawerContentsItemRenderer implements SpecialModelRenderer<ControllableDrawerTile<?>> {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(FunctionalStorage.MOD_ID, "drawer");

    @Override
    public void submit(@Nullable ControllableDrawerTile<?> tile, PoseStack poseStack, SubmitNodeCollector collector,
            int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (tile == null)
            return;
        poseStack.pushPose();
        if (tile instanceof FluidDrawerTile fluidTile) {
            DrawerContentsRenderer.submitFluids(poseStack, collector, lightCoords, fluidTile);
        } else if (tile instanceof DrawerTile drawerTile) {
            DrawerContentsRenderer.submitItems(poseStack, collector, lightCoords, drawerTile);
        } else if (tile instanceof CompactingDrawerTile compactingTile) {
            DrawerContentsRenderer.submitCompacting(poseStack, collector, lightCoords, compactingTile);
        } else if (tile instanceof SimpleCompactingDrawerTile simpleTile) {
            DrawerContentsRenderer.submitSimpleCompacting(poseStack, collector, lightCoords, simpleTile);
        }
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        // The block model layer of the composite gives the item its extents
    }

    @Override
    @Nullable
    public ControllableDrawerTile<?> extractArgument(ItemStack stack) {
        var level = Minecraft.getInstance().level;
        if (level == null || !(stack.getItem() instanceof DrawerBlockItem item))
            return null;
        return item.loadTile(stack, level.registryAccess());
    }

    public enum Unbaked implements SpecialModelRenderer.Unbaked<ControllableDrawerTile<?>> {
        INSTANCE;

        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public DrawerContentsItemRenderer bake(SpecialModelRenderer.BakingContext context) {
            return new DrawerContentsItemRenderer();
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
