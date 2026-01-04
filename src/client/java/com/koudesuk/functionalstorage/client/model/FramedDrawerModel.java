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

    private final ImmutableMap<String, UnbakedModel> children;
    private final ImmutableList<String> itemPasses;

    public FramedDrawerModel(ImmutableMap<String, UnbakedModel> children, ImmutableList<String> itemPasses) {
        this.children = children;
        this.itemPasses = itemPasses;
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return children.values().stream()
                .flatMap(model -> model.getDependencies().stream())
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> resolver) {
        for (UnbakedModel child : children.values()) {
            child.resolveParents(resolver);
        }
    }

    @Override
    public BakedModel bake(ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState state,
            ResourceLocation location) {
        ImmutableMap.Builder<String, BakedModel> bakedChildren = ImmutableMap.builder();
        BakedModel baseModel = null;

        for (Map.Entry<String, UnbakedModel> entry : children.entrySet()) {
            BakedModel baked = entry.getValue().bake(baker, spriteGetter, state, location);
            if (baked != null) {
                bakedChildren.put(entry.getKey(), baked);
                // Store the first "base" model to get transforms from
                if (entry.getKey().equals("base") || baseModel == null) {
                    baseModel = baked;
                }
            }
        }

        ImmutableMap<String, BakedModel> bakedMap = bakedChildren.build();

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
                itemPasses); // Pass the strings (keys) directly
    }
}
