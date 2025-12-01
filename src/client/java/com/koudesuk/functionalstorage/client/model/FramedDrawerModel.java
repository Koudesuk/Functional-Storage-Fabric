package com.koudesuk.functionalstorage.client.model;

import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

public class FramedDrawerModel implements UnbakedModel {

    private final ImmutableMap<String, ResourceLocation> children;
    private final ImmutableList<String> itemPasses;

    public FramedDrawerModel(ImmutableMap<String, ResourceLocation> children, ImmutableList<String> itemPasses) {
        this.children = children;
        this.itemPasses = itemPasses;
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return children.values();
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> resolver) {
        // Parents are resolved automatically by the baker when baking dependencies
    }

    @Override
    public BakedModel bake(ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState state,
            ResourceLocation location) {
        ImmutableMap.Builder<String, BakedModel> bakedChildren = ImmutableMap.builder();
        BakedModel baseModel = null;

        for (Map.Entry<String, ResourceLocation> entry : children.entrySet()) {
            BakedModel baked = baker.bake(entry.getValue(), state);
            if (baked != null) {
                bakedChildren.put(entry.getKey(), baked);
                // Store the first "base" model to get transforms from
                if (entry.getKey().equals("base") || baseModel == null) {
                    baseModel = baked;
                }
            }
        }

        ImmutableMap<String, BakedModel> bakedMap = bakedChildren.build();

        ImmutableList.Builder<BakedModel> passBuilder = ImmutableList.builder();
        for (String pass : itemPasses) {
            if (bakedMap.containsKey(pass)) {
                passBuilder.add(bakedMap.get(pass));
            }
        }

        TextureAtlasSprite particle = spriteGetter
                .apply(new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("minecraft", "missingno")));
        if (bakedMap.containsKey("particle")) {
            particle = bakedMap.get("particle").getParticleIcon();
        } else if (!bakedMap.isEmpty()) {
            particle = bakedMap.values().iterator().next().getParticleIcon();
        }

        // Get transforms from the base model instead of using NO_TRANSFORMS
        // This fixes the item display in inventory/hand
        ItemTransforms transforms = baseModel != null ? baseModel.getTransforms() : ItemTransforms.NO_TRANSFORMS;

        return new FramedDrawerBakedModel(
                true,
                true,
                true,
                particle,
                transforms, // Use transforms from base model
                bakedMap,
                passBuilder.build());
    }
}
