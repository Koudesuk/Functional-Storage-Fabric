package com.koudesuk.functionalstorage.client;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.client.gui.DrawerScreen;
import com.koudesuk.functionalstorage.client.model.FramedDrawerModelLoader;
import com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities;
import com.koudesuk.functionalstorage.registry.FunctionalStorageMenus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;

public class FunctionalStorageClient implements ClientModInitializer {
        @Override
        public void onInitializeClient() {
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
                                ctx -> new SimpleCompactingDrawerRenderer());
                BlockEntityRenderers.register(FunctionalStorageBlockEntities.FRAMED_SIMPLE_COMPACTING_DRAWER,
                                ctx -> new SimpleCompactingDrawerRenderer());
                BlockEntityRenderers.register(FunctionalStorageBlockEntities.COMPACTING_DRAWER,
                                ctx -> new CompactingDrawerRenderer());
                BlockEntityRenderers.register(FunctionalStorageBlockEntities.FRAMED_COMPACTING_DRAWER,
                                ctx -> new CompactingDrawerRenderer());

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

                // Register controller renderer for linking tool visualization
                BlockEntityRenderers.register(FunctionalStorageBlockEntities.STORAGE_CONTROLLER,
                                ctx -> new ControllerRenderer<>());
                BlockEntityRenderers.register(FunctionalStorageBlockEntities.FRAMED_DRAWER_CONTROLLER,
                                ctx -> new ControllerRenderer<>());

                registerMilkFluidRenderHandler();

                FramedColors.register();
        }

        private static void registerMilkFluidRenderHandler() {
                Material still = new Material(Identifier.withDefaultNamespace("block/water_still"));
                Material flowing = new Material(Identifier.withDefaultNamespace("block/water_flow"));
                FluidModel.Unbaked milkModel = new FluidModel.Unbaked(still, flowing, null, null);
                FluidRenderingRegistry.register(FunctionalStorage.MILK, FunctionalStorage.MILK_FLOWING, milkModel);
        }
}
