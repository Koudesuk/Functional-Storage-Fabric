package com.koudesuk.functionalstorage.client;

import com.koudesuk.functionalstorage.client.gui.DrawerScreen;
import com.koudesuk.functionalstorage.client.model.FramedDrawerModelLoader;
import com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities;
import com.koudesuk.functionalstorage.registry.FunctionalStorageBlocks;
import com.koudesuk.functionalstorage.registry.FunctionalStorageMenus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.gui.screens.MenuScreens;

public class FunctionalStorageClient implements ClientModInitializer {
        @Override
        public void onInitializeClient() {
                // This entrypoint is suitable for setting up client-specific logic, such as
                // rendering.
                ModelLoadingPlugin.register(new FramedDrawerModelLoader());
                MenuScreens.register(FunctionalStorageMenus.DRAWER, DrawerScreen::new);

                // Block entity renderers (drawers and compacting drawers share the same visuals
                // across framed variants)
                BlockEntityRendererRegistry.register(FunctionalStorageBlockEntities.DRAWER,
                                ctx -> new DrawerRenderer<>());
                BlockEntityRendererRegistry.register(FunctionalStorageBlockEntities.FRAMED_DRAWER,
                                ctx -> new DrawerRenderer<>());
                BlockEntityRendererRegistry.register(FunctionalStorageBlockEntities.SIMPLE_COMPACTING_DRAWER,
                                ctx -> new SimpleCompactingDrawerRenderer());
                BlockEntityRendererRegistry.register(FunctionalStorageBlockEntities.FRAMED_SIMPLE_COMPACTING_DRAWER,
                                ctx -> new SimpleCompactingDrawerRenderer());
                BlockEntityRendererRegistry.register(FunctionalStorageBlockEntities.COMPACTING_DRAWER,
                                ctx -> new CompactingDrawerRenderer());
                BlockEntityRendererRegistry.register(FunctionalStorageBlockEntities.FRAMED_COMPACTING_DRAWER,
                                ctx -> new CompactingDrawerRenderer());

                // Fluid drawer renderers
                BlockEntityRendererRegistry.register(FunctionalStorageBlockEntities.FLUID_DRAWER_1,
                                ctx -> new FluidDrawerRenderer());
                BlockEntityRendererRegistry.register(FunctionalStorageBlockEntities.FLUID_DRAWER_2,
                                ctx -> new FluidDrawerRenderer());
                BlockEntityRendererRegistry.register(FunctionalStorageBlockEntities.FLUID_DRAWER_4,
                                ctx -> new FluidDrawerRenderer());

                // Block render layers
                FunctionalStorageBlocks.DRAWER_TYPES.values().forEach(blocks -> blocks
                                .forEach(block -> BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.cutout())));
                BlockRenderLayerMap.INSTANCE.putBlock(FunctionalStorageBlocks.COMPACTING_DRAWER, RenderType.cutout());
                BlockRenderLayerMap.INSTANCE.putBlock(FunctionalStorageBlocks.FRAMED_COMPACTING_DRAWER,
                                RenderType.cutout());
                BlockRenderLayerMap.INSTANCE.putBlock(FunctionalStorageBlocks.ENDER_DRAWER, RenderType.cutout());
                BlockRenderLayerMap.INSTANCE.putBlock(FunctionalStorageBlocks.SIMPLE_COMPACTING_DRAWER,
                                RenderType.cutout());
                BlockRenderLayerMap.INSTANCE.putBlock(FunctionalStorageBlocks.FRAMED_SIMPLE_COMPACTING_DRAWER,
                                RenderType.cutout());
                BlockRenderLayerMap.INSTANCE.putBlock(FunctionalStorageBlocks.DRAWER_CONTROLLER, RenderType.cutout());
                BlockRenderLayerMap.INSTANCE.putBlock(FunctionalStorageBlocks.FRAMED_DRAWER_CONTROLLER,
                                RenderType.cutout());
                BlockRenderLayerMap.INSTANCE.putBlock(FunctionalStorageBlocks.CONTROLLER_EXTENSION,
                                RenderType.cutout());
                BlockRenderLayerMap.INSTANCE.putBlock(FunctionalStorageBlocks.ARMORY_CABINET, RenderType.cutout());

                // Fluid drawer blocks - use translucent for fluid rendering
                BlockRenderLayerMap.INSTANCE.putBlock(FunctionalStorageBlocks.FLUID_DRAWER_1, RenderType.translucent());
                BlockRenderLayerMap.INSTANCE.putBlock(FunctionalStorageBlocks.FLUID_DRAWER_2, RenderType.translucent());
                BlockRenderLayerMap.INSTANCE.putBlock(FunctionalStorageBlocks.FLUID_DRAWER_4, RenderType.translucent());

                // Register controller renderer for linking tool visualization
                BlockEntityRendererRegistry.register(FunctionalStorageBlockEntities.STORAGE_CONTROLLER,
                                ctx -> new ControllerRenderer<>());
                BlockEntityRendererRegistry.register(FunctionalStorageBlockEntities.FRAMED_DRAWER_CONTROLLER,
                                ctx -> new ControllerRenderer<>());

                FramedColors.register();

                // NOTE: Dynamic item texture rendering for framed drawers is handled by
                // FramedDrawerBakedModel.emitItemQuads() when the model uses the custom loader.
                // The item models inherit from block models which use the custom loader,
                // so NBT-based textures should work when the baked model is properly resolved.
        }
}
