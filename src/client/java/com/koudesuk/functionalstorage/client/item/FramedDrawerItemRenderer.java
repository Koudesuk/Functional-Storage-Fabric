package com.koudesuk.functionalstorage.client.item;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.block.FramedBlock;
import com.koudesuk.functionalstorage.block.FramedDrawerBlock;
import com.koudesuk.functionalstorage.client.model.FramedDrawerModel;
import com.koudesuk.functionalstorage.client.model.FramedDrawerModelData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableMesh;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Draws framed items with the design stored on the stack, a port of the original's
 * FramedSpecialRenderer: the block's own model in its default state, retextured like the placed
 * block. It goes out as a Fabric mesh because vanilla item quads carry no colors for the tints.
 * The items/*.json of the framed items use it as "functionalstorage:framed".
 */
public final class FramedDrawerItemRenderer implements SpecialModelRenderer<FramedDrawerItemRenderer.RenderData> {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(FunctionalStorage.MOD_ID, "framed");
    private static final int[] NO_TINTS = new int[16];

    static {
        Arrays.fill(NO_TINTS, 0xFFFFFFFF);
    }

    @Override
    public void submit(@Nullable RenderData data, PoseStack poseStack, SubmitNodeCollector collector, int lightCoords,
            int overlayCoords, boolean hasFoil, int outlineColor) {
        if (data == null)
            return;
        BlockState state = data.block().defaultBlockState();
        List<BlockStateModelPart> parts = new ArrayList<>();
        Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state)
                .collectParts(RandomSource.create(42), parts);
        MutableMesh mesh = Renderer.get().mutableMesh();
        FramedDrawerModel.emitParts(mesh.emitter(), parts, data.design(), BlockAndTintGetter.EMPTY, BlockPos.ZERO,
                face -> false, true);
        collector.submitItem(poseStack, ItemDisplayContext.NONE, lightCoords, overlayCoords, outlineColor, NO_TINTS,
                List.of(), mesh, hasFoil ? ItemStackRenderState.FoilType.STANDARD : ItemStackRenderState.FoilType.NONE);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        for (int x = 0; x <= 1; x++) {
            for (int y = 0; y <= 1; y++) {
                for (int z = 0; z <= 1; z++)
                    output.accept(new Vector3f(x, y, z));
            }
        }
    }

    @Override
    @Nullable
    public RenderData extractArgument(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem) || !(blockItem.getBlock() instanceof FramedBlock))
            return null;
        FramedDrawerModelData design = FramedDrawerBlock.getDrawerModelData(stack);
        return new RenderData(blockItem.getBlock(), design == null || design.getDesign().isEmpty() ? null : design);
    }

    public record RenderData(Block block, @Nullable FramedDrawerModelData design) {
    }

    public enum Unbaked implements SpecialModelRenderer.Unbaked<RenderData> {
        INSTANCE;

        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public FramedDrawerItemRenderer bake(SpecialModelRenderer.BakingContext context) {
            return new FramedDrawerItemRenderer();
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
