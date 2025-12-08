package com.koudesuk.functionalstorage.client.model;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Model loading plugin for Framed Drawer custom models.
 * Registers a resolver that provides FramedDrawerModel instances for models
 * with the custom "functionalstorage:framed_drawer" loader.
 */
public class FramedDrawerModelLoader implements ModelLoadingPlugin {

    @Override
    public void onInitializeModelLoader(Context pluginContext) {
        FunctionalStorage.LOGGER.info("Registering Framed Drawer model loader...");

        // Register a model resolver that will provide our custom model
        // when the JSON specifies "loader": "functionalstorage:framed_drawer"
        pluginContext.resolveModel().register(context -> {
            // Get the model identifier
            ResourceLocation id = context.id();

            if (!id.getNamespace().equals(FunctionalStorage.MOD_ID))
                return null;

            // Try to load the JSON directly to check the loader field
            // Model paths are usually models/ID.json
            ResourceLocation jsonId = new ResourceLocation(id.getNamespace(), "models/" + id.getPath() + ".json");

            try {
                Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(jsonId);
                if (resource.isPresent()) {
                    try (Reader reader = resource.get().openAsReader()) {
                        JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                        if (json.has("loader")) {
                            String loaderId = json.get("loader").getAsString();
                            if (!loaderId.equals("functionalstorage:framed_drawer")
                                    && !loaderId.equals("functionalstorage:framedblock")) {
                                return null;
                            }
                        } else {
                            return null;
                        }

                        ImmutableMap.Builder<String, ResourceLocation> children = ImmutableMap.builder();
                        List<String> itemPasses = new ArrayList<>();

                        if (json.has("parent")) {
                            children.put("base", new ResourceLocation(json.get("parent").getAsString()));
                            itemPasses.add("base");
                        }

                        if (json.has("children")) {
                            JsonObject childrenObj = json.getAsJsonObject("children");
                            for (Map.Entry<String, JsonElement> entry : childrenObj.entrySet()) {
                                JsonElement value = entry.getValue();
                                if (value.isJsonPrimitive()) {
                                    // Simple string reference to a model
                                    children.put(entry.getKey(), new ResourceLocation(value.getAsString()));
                                    itemPasses.add(entry.getKey());
                                } else if (value.isJsonObject()) {
                                    // Forge-style embedded BlockModel object with parent
                                    JsonObject childModel = value.getAsJsonObject();
                                    if (childModel.has("parent")) {
                                        children.put(entry.getKey(),
                                                new ResourceLocation(childModel.get("parent").getAsString()));
                                        itemPasses.add(entry.getKey());
                                    }
                                }
                            }
                        }

                        if (json.has("item_render_order")) {
                            itemPasses.clear();
                            for (JsonElement e : json.getAsJsonArray("item_render_order")) {
                                itemPasses.add(e.getAsString());
                            }
                        }

                        return new FramedDrawerModel(children.build(), ImmutableList.copyOf(itemPasses));
                    }
                }
            } catch (Exception e) {
                // Ignore errors, let vanilla handle it or fail gracefully
            }

            return null;
        });
    }
}
