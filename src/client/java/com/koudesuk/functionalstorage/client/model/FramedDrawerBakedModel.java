package com.koudesuk.functionalstorage.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;
import java.util.Collections;

public class FramedDrawerBakedModel implements BakedModel, FabricBakedModel {

    private final boolean isGui3d;
    private final boolean useBlockLight;
    private final boolean useAmbientOcclusion;
    private final TextureAtlasSprite particle;
    private final ItemTransforms transforms;
    private final ItemOverrides overrides;
    private final ImmutableMap<String, BakedModel> children;
    private final ImmutableList<BakedModel> itemPasses;

    public FramedDrawerBakedModel(boolean isGui3d, boolean useBlockLight, boolean useAmbientOcclusion,
            TextureAtlasSprite particle, ItemTransforms transforms,
            ImmutableMap<String, BakedModel> children, ImmutableList<BakedModel> itemPasses) {
        this.isGui3d = isGui3d;
        this.useBlockLight = useBlockLight;
        this.useAmbientOcclusion = useAmbientOcclusion;
        this.particle = particle;
        this.transforms = transforms;
        this.overrides = createItemOverrides();
        this.children = children;
        this.itemPasses = itemPasses;
    }

    /**
     * Creates a custom ItemOverrides that forces the model to re-render when the ItemStack's NBT changes.
     * This is necessary because Fabric's FabricBakedModel.emitItemQuads() is called each frame,
     * but Minecraft caches the model based on ItemOverrides.resolve() result.
     * By returning 'this' (the parent model) each time, we ensure emitItemQuads() is called
     * with the current ItemStack, allowing dynamic texture updates based on NBT.
     *
     * Uses the public constructor ItemOverrides(ModelBaker, BlockModel, List<ItemOverride>)
     * since the no-arg constructor is private.
     */
    private ItemOverrides createItemOverrides() {
        return new ItemOverrides(null, null, Collections.emptyList()) {
            @Override
            public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level,
                    @Nullable LivingEntity entity, int seed) {
                // Always return the parent model to ensure emitItemQuads() is called with the current ItemStack
                // The actual texture resolution happens in emitItemQuads() based on the NBT data
                return FramedDrawerBakedModel.this;
            }
        };
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        List<BakedQuad> quads = new ArrayList<>();
        for (BakedModel child : children.values()) {
            quads.addAll(child.getQuads(state, side, rand));
        }
        return quads;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos,
            Supplier<RandomSource> randomSupplier, RenderContext context) {
        FramedDrawerModelData data = null;
        if (blockView instanceof RenderAttachedBlockView attachedView) {
            Object attachment = attachedView.getBlockEntityRenderAttachment(pos);
            if (attachment instanceof FramedDrawerModelData) {
                data = (FramedDrawerModelData) attachment;
            }
        }

        // If no custom texture data, just render base models without retexturing
        if (data == null || data.getDesign().isEmpty()) {
            if (children != null) {
                for (BakedModel childModel : children.values()) {
                    if (childModel != null) {
                        emitVanillaQuads(childModel, state, randomSupplier, context);
                    }
                }
            }
            return;
        }

        // Has custom texture data, proceed with retexturing
        if (children != null) {
            for (Map.Entry<String, BakedModel> entry : children.entrySet()) {
                BakedModel childModel = entry.getValue();
                if (childModel != null) {
                    emitRetexturedQuads(childModel, data, state, pos, randomSupplier, context);
                }
            }
        }
    }

    private void emitRetexturedQuads(BakedModel shapeModel, FramedDrawerModelData data, BlockState state, BlockPos pos,
            Supplier<RandomSource> randomSupplier, RenderContext context) {

        QuadEmitter emitter = context.getEmitter();
        RandomSource rand = randomSupplier.get();

        for (Direction dir : new Direction[] { null, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH,
                Direction.WEST, Direction.EAST }) {
            List<BakedQuad> quads = shapeModel.getQuads(state, dir, rand);

            for (BakedQuad quad : quads) {
                TextureAtlasSprite originalSprite = quad.getSprite();
                TextureAtlasSprite newSprite = originalSprite;
                int tintColor = -1;

                if (data != null && !data.getDesign().isEmpty()) {
                    String spriteName = originalSprite.contents().name().toString();
                    String part = determineModelPart(spriteName);

                    if (part != null && data.getDesign().containsKey(part)) {
                        Item item = data.getDesign().get(part);
                        if (item instanceof BlockItem blockItem) {
                            BlockState frameState = blockItem.getBlock().defaultBlockState();
                            BakedModel frameModel = Minecraft.getInstance().getBlockRenderer()
                                    .getBlockModel(frameState);

                            List<BakedQuad> frameQuads = frameModel.getQuads(frameState, quad.getDirection(), rand);
                            if (frameQuads.isEmpty() && quad.getDirection() != null) {
                                frameQuads = frameModel.getQuads(frameState, null, rand);
                            }

                            if (!frameQuads.isEmpty()) {
                                BakedQuad frameQuad = frameQuads.get(0);
                                newSprite = frameQuad.getSprite();

                                if (frameQuad.isTinted()) {
                                    tintColor = Minecraft.getInstance().getBlockColors().getColor(frameState, null,
                                            null, frameQuad.getTintIndex());
                                }
                            } else {
                                newSprite = frameModel.getParticleIcon();
                            }
                        }
                    }
                }

                emitter.fromVanilla(quad, null, dir);

                if (newSprite != originalSprite) {
                    emitter.spriteBake(newSprite, MutableQuadView.BAKE_LOCK_UV);
                }

                if (tintColor != -1) {
                    emitter.color(tintColor, tintColor, tintColor, tintColor);
                }

                emitter.emit();
            }
        }
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
        // Check if item has custom texture data
        FramedDrawerModelData data = null;
        if (stack.hasTag() && stack.getTag().contains("Style")) {
            data = FramedDrawerModelData.fromNBT(stack.getTag().getCompound("Style"));
        }

        // If no Style tag or empty data, render base models without retexturing
        if (data == null || data.getDesign().isEmpty()) {
            if (itemPasses != null) {
                for (BakedModel model : itemPasses) {
                    if (model != null) {
                        emitVanillaItemQuads(model, randomSupplier, context);
                    }
                }
            }
            return;
        }

        // Has custom texture data, proceed with retexturing
        if (itemPasses != null) {
            for (BakedModel model : itemPasses) {
                if (model != null) {
                    emitRetexturedItemQuads(model, data, stack, randomSupplier, context);
                }
            }
        }
    }

    private void emitRetexturedItemQuads(BakedModel shapeModel, FramedDrawerModelData data, ItemStack stack,
            Supplier<RandomSource> randomSupplier, RenderContext context) {

        QuadEmitter emitter = context.getEmitter();
        RandomSource rand = randomSupplier.get();

        for (Direction dir : new Direction[] { null, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH,
                Direction.WEST, Direction.EAST }) {
            List<BakedQuad> quads = shapeModel.getQuads(null, dir, rand);

            for (BakedQuad quad : quads) {
                TextureAtlasSprite originalSprite = quad.getSprite();
                TextureAtlasSprite newSprite = originalSprite;
                int tintColor = -1;

                if (data != null && !data.getDesign().isEmpty()) {
                    String spriteName = originalSprite.contents().name().toString();
                    String part = determineModelPart(spriteName);

                    if (part != null && data.getDesign().containsKey(part)) {
                        Item item = data.getDesign().get(part);
                        if (item instanceof BlockItem blockItem) {
                            BlockState frameState = blockItem.getBlock().defaultBlockState();
                            BakedModel frameModel = Minecraft.getInstance().getBlockRenderer()
                                    .getBlockModel(frameState);

                            List<BakedQuad> frameQuads = frameModel.getQuads(frameState, quad.getDirection(), rand);
                            if (frameQuads.isEmpty() && quad.getDirection() != null) {
                                frameQuads = frameModel.getQuads(frameState, null, rand);
                            }

                            if (!frameQuads.isEmpty()) {
                                newSprite = frameQuads.get(0).getSprite();
                            } else {
                                newSprite = frameModel.getParticleIcon();
                            }
                        }
                    }
                }

                emitter.fromVanilla(quad, null, dir);

                if (newSprite != originalSprite) {
                    emitter.spriteBake(newSprite, MutableQuadView.BAKE_LOCK_UV);
                }

                if (tintColor != -1) {
                    emitter.color(tintColor, tintColor, tintColor, tintColor);
                }

                emitter.emit();
            }
        }
    }

    private String determineModelPart(String spriteName) {
        String lowerName = spriteName.toLowerCase();

        if (lowerName.contains("framed_front") ||
                lowerName.contains("framed_controller_front") ||
                lowerName.contains("_front") && lowerName.contains("framed")) {
            return "front";
        }

        if (lowerName.contains("framed_side") ||
                lowerName.contains("framed_controller_side") ||
                lowerName.contains("framed_top") ||
                lowerName.contains("_side") && lowerName.contains("framed") ||
                lowerName.contains("_top") && lowerName.contains("framed")) {
            return "side";
        }

        if (lowerName.contains("framed_trim") ||
                lowerName.contains("framed_divider") ||
                lowerName.contains("_trim") && lowerName.contains("framed") ||
                lowerName.contains("_divider") && lowerName.contains("framed")) {
            return "front_divider";
        }

        if (lowerName.contains("framed")) {
            return "particle";
        }

        return null;
    }

    /**
     * Emit quads from a model without any retexturing (fallback for when no custom
     * texture data exists).
     */
    private void emitVanillaQuads(BakedModel model, BlockState state,
            Supplier<RandomSource> randomSupplier, RenderContext context) {
        QuadEmitter emitter = context.getEmitter();
        RandomSource rand = randomSupplier.get();

        for (Direction dir : new Direction[] { null, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH,
                Direction.WEST, Direction.EAST }) {
            List<BakedQuad> quads = model.getQuads(state, dir, rand);
            for (BakedQuad quad : quads) {
                emitter.fromVanilla(quad, null, dir);
                emitter.emit();
            }
        }
    }

    /**
     * Emit item quads without retexturing (fallback for items without custom
     * texture data).
     */
    private void emitVanillaItemQuads(BakedModel model, Supplier<RandomSource> randomSupplier, RenderContext context) {
        QuadEmitter emitter = context.getEmitter();
        RandomSource rand = randomSupplier.get();

        for (Direction dir : new Direction[] { null, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH,
                Direction.WEST, Direction.EAST }) {
            List<BakedQuad> quads = model.getQuads(null, dir, rand);
            for (BakedQuad quad : quads) {
                emitter.fromVanilla(quad, null, dir);
                emitter.emit();
            }
        }
    }

    @Override
    public boolean useAmbientOcclusion() {
        return useAmbientOcclusion;
    }

    @Override
    public boolean isGui3d() {
        return isGui3d;
    }

    @Override
    public boolean usesBlockLight() {
        return useBlockLight;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return particle;
    }

    @Override
    public ItemTransforms getTransforms() {
        return transforms;
    }

    @Override
    public ItemOverrides getOverrides() {
        return overrides;
    }
}
