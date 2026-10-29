package com.koudesuk.functionalstorage.client.model;

import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * The baked model of every framed block state, wrapped by FramedDrawerModelLoader: draws the block
 * with the textures of its design, a port of the original's FramedModel (a NeoForge
 * DynamicBlockStateModel) to the Fabric renderer. Without a design it draws exactly what it wraps.
 */
public final class FramedDrawerBakedModel extends WrapperBlockStateModel {

    public FramedDrawerBakedModel(BlockStateModel wrapped) {
        super(wrapped);
    }

    /**
     * The design of the framed tile at this position, from its getRenderData().
     */
    @Nullable
    private static FramedDrawerModelData design(BlockAndTintGetter level, BlockPos pos) {
        return level.getBlockEntityRenderData(pos) instanceof FramedDrawerModelData data
                && !data.getDesign().isEmpty() ? data : null;
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state,
            RandomSource random, Predicate<Direction> cullTest) {
        FramedDrawerModelData design = design(level, pos);
        if (design == null) {
            super.emitQuads(emitter, level, pos, state, random, cullTest);
            return;
        }
        List<BlockStateModelPart> parts = new ArrayList<>();
        wrapped.collectParts(random, parts);
        FramedDrawerModel.emitParts(emitter, parts, design, level, pos, cullTest, false);
    }

    @Override
    @Nullable
    public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        Object key = super.createGeometryKey(level, pos, state, random);
        FramedDrawerModelData design = design(level, pos);
        return design == null || key == null ? key : new GeometryKey(key, design.getCode());
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        BlockItem particle = FramedDrawerModel.material(design(level, pos), "particle");
        if (particle != null) {
            BlockState material = particle.getBlock().defaultBlockState();
            return FramedDrawerModel.modelOf(material).particleMaterial(level, pos, material);
        }
        return super.particleMaterial(level, pos, state);
    }

    /**
     * The flags of the frame plus those of every block in the design, like the original, so a
     * translucent or animated material is treated as such.
     */
    @Override
    public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        int flags = super.materialFlags(level, pos, state, random);
        FramedDrawerModelData design = design(level, pos);
        if (design != null) {
            for (String key : design.getDesign().keySet()) {
                BlockItem material = FramedDrawerModel.material(design, key);
                if (material != null) {
                    BlockState materialState = material.getBlock().defaultBlockState();
                    flags |= FramedDrawerModel.modelOf(materialState).materialFlags(level, pos, materialState, random);
                }
            }
        }
        return flags;
    }

    @Override
    public boolean hasMaterialFlag(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
            int flag) {
        return (materialFlags(level, pos, state, random) & flag) != 0;
    }

    private record GeometryKey(Object delegate, String design) {
    }
}
