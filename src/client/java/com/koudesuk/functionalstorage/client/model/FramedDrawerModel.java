package com.koudesuk.functionalstorage.client.model;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.block.FramedBlock;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.ShadeMode;
import net.fabricmc.fabric.api.client.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Retexturing shared by framed blocks (FramedDrawerBakedModel) and framed items
 * (FramedDrawerItemRenderer), a port of the original's FramedModel: every quad drawn with a framed
 * marker texture takes the texture of the block chosen for that part, everything else is drawn as is.
 */
public final class FramedDrawerModel {

    private FramedDrawerModel() {
    }

    /**
     * The design part a quad stands for, read from its texture.
     */
    @Nullable
    static String markerKey(BakedQuad quad) {
        Identifier texture = quad.materialInfo().sprite().contents().name();
        if (!texture.getNamespace().equals(FunctionalStorage.MOD_ID))
            return null;
        String path = texture.getPath();
        if (path.equals("block/framed_side"))
            return "side";
        // The original's divider marker is machine_divider, the Fabric models use framed_divider
        if (path.equals("block/framed_divider") || path.equals("block/machine_divider"))
            return "front_divider";
        if (path.startsWith("block/framed_front_"))
            return "front";
        return null;
    }

    /**
     * The block chosen for a part, or null. Framed blocks are never used as a material.
     */
    @Nullable
    static BlockItem material(@Nullable FramedDrawerModelData design, String key) {
        if (design == null)
            return null;
        return design.getDesign().get(key) instanceof BlockItem blockItem
                && !(blockItem.getBlock() instanceof FramedBlock) ? blockItem : null;
    }

    static BlockStateModel modelOf(BlockState state) {
        return Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
    }

    /**
     * Emits the parts with the design applied, skipping culled faces like Fabric's own part
     * encoder. A marker quad becomes one quad per face the chosen block has on that side, so
     * materials with overlays (grass) keep them, like the original.
     */
    public static void emitParts(QuadEmitter emitter, List<BlockStateModelPart> parts,
            @Nullable FramedDrawerModelData design, BlockAndTintGetter level, BlockPos pos,
            Predicate<Direction> cullTest, boolean item) {
        Map<MaterialKey, List<BakedQuad>> materials = new HashMap<>();
        for (BlockStateModelPart part : parts) {
            TriState ao = part.useAmbientOcclusion() ? TriState.DEFAULT : TriState.FALSE;
            for (int i = 0; i <= ModelHelper.NULL_FACE_ID; i++) {
                Direction cullFace = ModelHelper.faceFromIndex(i);
                if (cullTest.test(cullFace))
                    continue;
                for (BakedQuad shape : part.getQuads(cullFace)) {
                    String key = markerKey(shape);
                    BlockItem material = key == null ? null : material(design, key);
                    // A divider without its own block looks like the sides, like the original
                    if (material == null && "front_divider".equals(key))
                        material = material(design, "side");
                    List<BakedQuad> samples = material == null ? List.of()
                            : materials.computeIfAbsent(new MaterialKey(material, shape.direction()),
                                    k -> materialQuads(k.item(), k.direction()));
                    if (samples.isEmpty()) {
                        begin(emitter, shape, cullFace, ao);
                        emitter.emit();
                        continue;
                    }
                    for (BakedQuad sample : samples) {
                        begin(emitter, shape, cullFace, ao);
                        reskin(emitter, shape, sample, material, level, pos, item);
                        emitter.emit();
                    }
                }
            }
        }
    }

    private static void begin(QuadEmitter emitter, BakedQuad shape, @Nullable Direction cullFace, TriState ao) {
        emitter.cullFace(cullFace);
        emitter.fromBakedQuad(shape);
        emitter.ambientOcclusion(ao);
        emitter.shadeMode(ShadeMode.VANILLA);
    }

    /**
     * The faces the block's model has on this side, falling back to its unculled faces pointing
     * that way.
     */
    private static List<BakedQuad> materialQuads(BlockItem item, Direction direction) {
        BlockState state = item.getBlock().defaultBlockState();
        List<BlockStateModelPart> parts = new ArrayList<>();
        modelOf(state).collectParts(RandomSource.create(42), parts);
        List<BakedQuad> quads = new ArrayList<>();
        for (BlockStateModelPart part : parts)
            quads.addAll(part.getQuads(direction));
        if (quads.isEmpty()) {
            for (BlockStateModelPart part : parts) {
                for (BakedQuad quad : part.getQuads(null)) {
                    if (quad.direction() == direction)
                        quads.add(quad);
                }
            }
        }
        return quads;
    }

    /**
     * Moves the quad's texture to the same spot of the sample's sprite (what NeoForge's
     * MutableQuad.setSpriteAndMoveUv does for the original), then applies the material's tint and
     * light. Vanilla quads carry no colors, so the tint is baked into the vertex colors.
     */
    private static void reskin(MutableQuadView quad, BakedQuad shape, BakedQuad sample, BlockItem material,
            BlockAndTintGetter level, BlockPos pos, boolean item) {
        TextureAtlasSprite from = shape.materialInfo().sprite();
        TextureAtlasSprite to = sample.materialInfo().sprite();
        for (int vertex = 0; vertex < 4; vertex++) {
            float u = (quad.u(vertex) - from.getU0()) / (from.getU1() - from.getU0());
            float v = (quad.v(vertex) - from.getV0()) / (from.getV1() - from.getV0());
            quad.uv(vertex, to.getU0() + u * (to.getU1() - to.getU0()), to.getV0() + v * (to.getV1() - to.getV0()));
        }
        quad.chunkLayer(sample.materialInfo().layer());
        quad.itemRenderType(sample.materialInfo().itemRenderType());
        quad.animated(to.contents().isAnimated());
        quad.tintIndex(-1);

        BlockState state = material.getBlock().defaultBlockState();
        int tintIndex = sample.materialInfo().tintIndex();
        if (tintIndex >= 0) {
            BlockTintSource tint = Minecraft.getInstance().getBlockColors().getTintSource(state, tintIndex);
            if (tint != null) {
                int color = item ? tint.color(state) : tint.colorInWorld(state, level, pos);
                quad.multiplyColor(0xFF000000 | color);
            }
        }
        int emission = Math.max(sample.materialInfo().lightEmission(), state.getLightEmission());
        if (emission > 0)
            quad.minLightmap(LightCoordsUtil.lightCoordsWithEmission(0, emission));
    }

    private record MaterialKey(BlockItem item, Direction direction) {
    }
}
