package com.koudesuk.functionalstorage.client.item;

import com.koudesuk.functionalstorage.client.model.FramedDrawerModelData;
import com.koudesuk.functionalstorage.util.DrawerType;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import com.mojang.blaze3d.vertex.PoseStack;

import java.util.HashMap;

/**
 * Fabric client-side item renderer for framed drawers.
 * Equivalent to Forge's DrawerISTER (BlockEntityWithoutLevelRenderer).
 * Reads NBT from ItemStack and renders with custom textures.
 */
public class FramedDrawerItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

    private final DrawerType type;
    private final BlockState defaultBlockState;

    public FramedDrawerItemRenderer(DrawerType type, BlockState blockState) {
        this.type = type;
        this.defaultBlockState = blockState;
    }

    @Override
    public void render(ItemStack stack, ItemDisplayContext mode, PoseStack matrices,
            MultiBufferSource vertexConsumers, int light, int overlay) {

        Minecraft minecraft = Minecraft.getInstance();
        BlockRenderDispatcher blockRenderer = minecraft.getBlockRenderer();

        // Get the block model
        BakedModel model = blockRenderer.getBlockModel(defaultBlockState);

        // Read NBT data if present
        FramedDrawerModelData modelData = null;
        if (stack.hasTag() && stack.getTag().contains("Style")) {
            modelData = FramedDrawerModelData.fromNBT(stack.getTag().getCompound("Style"));
        }

        // Render the model
        // If we have custom texture data, it will be read by the
        // FramedDrawerBakedModel's emitItemQuads
        matrices.pushPose();

        // Apply standard item transformations
        matrices.translate(0.5, 0.5, 0.5);

        // Render using block renderer
        blockRenderer.renderSingleBlock(defaultBlockState, matrices, vertexConsumers, light, overlay);

        matrices.popPose();
    }
}
