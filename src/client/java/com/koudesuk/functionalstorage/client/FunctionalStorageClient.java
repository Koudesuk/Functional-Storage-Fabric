package com.koudesuk.functionalstorage.client;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.block.config.FunctionalStorageConfig;
import com.koudesuk.functionalstorage.client.gui.DrawerScreen;
import com.koudesuk.functionalstorage.network.ConfigSyncPayload;
import com.koudesuk.functionalstorage.client.model.FramedDrawerModelLoader;
import com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities;
import com.koudesuk.functionalstorage.registry.FunctionalStorageBlocks;
import com.koudesuk.functionalstorage.registry.FunctionalStorageMenus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.gui.screens.MenuScreens;

public class FunctionalStorageClient implements ClientModInitializer {
        private static boolean usingServerConfig;

        @Override
        public void onInitializeClient() {
                FunctionalStorageClientConfig.FILE.load();
                DrawerClickHandler.register();

                // Use a remote server's common config while connected to it. A singleplayer or LAN
                // host shares its config with the integrated server already.
                ClientPlayNetworking.registerGlobalReceiver(ConfigSyncPayload.TYPE, (payload, context) -> {
                        if (!context.client().hasSingleplayerServer()) {
                                FunctionalStorageConfig.FILE.apply(payload.values());
                                usingServerConfig = true;
                        }
                });
                ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
                        if (usingServerConfig) {
                                usingServerConfig = false;
                                FunctionalStorageConfig.FILE.load();
                        }
                });

                // This entrypoint is suitable for setting up client-specific logic, such as
                // rendering.
                ModelLoadingPlugin.register(new FramedDrawerModelLoader());
                MenuScreens.register(FunctionalStorageMenus.DRAWER, DrawerScreen::new);

                // Block entity renderers (drawers and compacting drawers share the same visuals
                // across framed variants)
                BlockEntityRenderers.register(FunctionalStorageBlockEntities.DRAWER,
                                ctx -> new DrawerRenderer<>());
                BlockEntityRenderers.register(FunctionalStorageBlockEntities.FRAMED_DRAWER,
                                ctx -> new DrawerRenderer<>());
                BlockEntityRenderers.register(FunctionalStorageBlockEntities.SIMPLE_COMPACTING_DRAWER,
                                ctx -> new SimpleCompactingDrawerRenderer<>());
                BlockEntityRenderers.register(FunctionalStorageBlockEntities.FRAMED_SIMPLE_COMPACTING_DRAWER,
                                ctx -> new SimpleCompactingDrawerRenderer<>());
                BlockEntityRenderers.register(FunctionalStorageBlockEntities.COMPACTING_DRAWER,
                                ctx -> new CompactingDrawerRenderer<>());
                BlockEntityRenderers.register(FunctionalStorageBlockEntities.FRAMED_COMPACTING_DRAWER,
                                ctx -> new CompactingDrawerRenderer<>());

                // Fluid drawer renderers
                BlockEntityRenderers.register(FunctionalStorageBlockEntities.FLUID_DRAWER_1,
                                ctx -> new FluidDrawerRenderer());
                BlockEntityRenderers.register(FunctionalStorageBlockEntities.FLUID_DRAWER_2,
                                ctx -> new FluidDrawerRenderer());
                BlockEntityRenderers.register(FunctionalStorageBlockEntities.FLUID_DRAWER_4,
                                ctx -> new FluidDrawerRenderer());

                // Ender drawer renderer
                BlockEntityRenderers.register(FunctionalStorageBlockEntities.ENDER_DRAWER,
                                ctx -> new EnderDrawerRenderer());

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
                BlockRenderLayerMap.INSTANCE.putBlock(FunctionalStorageBlocks.FRAMED_CONTROLLER_EXTENSION,
                                RenderType.cutout());
                BlockRenderLayerMap.INSTANCE.putBlock(FunctionalStorageBlocks.ARMORY_CABINET, RenderType.cutout());

                // Fluid drawer blocks - use cutout (like Forge) to avoid depth/transparency
                // issues
                BlockRenderLayerMap.INSTANCE.putBlock(FunctionalStorageBlocks.FLUID_DRAWER_1, RenderType.cutout());
                BlockRenderLayerMap.INSTANCE.putBlock(FunctionalStorageBlocks.FLUID_DRAWER_2, RenderType.cutout());
                BlockRenderLayerMap.INSTANCE.putBlock(FunctionalStorageBlocks.FLUID_DRAWER_4, RenderType.cutout());

                // Register controller renderer for linking tool visualization
                BlockEntityRenderers.register(FunctionalStorageBlockEntities.STORAGE_CONTROLLER,
                                ctx -> new ControllerRenderer<>());
                BlockEntityRenderers.register(FunctionalStorageBlockEntities.FRAMED_DRAWER_CONTROLLER,
                                ctx -> new ControllerRenderer<>());

                var milkRenderHandler = SimpleFluidRenderHandler.coloredWater(0xFFFFFFFF);
                FluidRenderHandlerRegistry.INSTANCE.register(FunctionalStorage.MILK, milkRenderHandler);
                FluidRenderHandlerRegistry.INSTANCE.register(FunctionalStorage.MILK_FLOWING, milkRenderHandler);

                FramedColors.register();

                // NOTE: Dynamic item texture rendering for framed drawers is handled by
                // FramedDrawerBakedModel.emitItemQuads() when the model uses the custom loader.
                // The item models inherit from block models which use the custom loader,
                // so NBT-based textures should work when the baked model is properly resolved.
        }
}
