package com.koudesuk.functionalstorage.client;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.block.config.FunctionalStorageConfig;
import com.koudesuk.functionalstorage.client.gui.DrawerScreen;
import com.koudesuk.functionalstorage.client.item.DrawerContentsItemRenderer;
import com.koudesuk.functionalstorage.client.item.FramedDrawerItemRenderer;
import com.koudesuk.functionalstorage.client.model.FramedDrawerModelLoader;
import com.koudesuk.functionalstorage.item.DrawerContentsTooltip;
import com.koudesuk.functionalstorage.item.UpgradeItem;
import com.koudesuk.functionalstorage.item.component.FunctionalUpgradeBehavior;
import com.koudesuk.functionalstorage.network.ConfigSyncPayload;
import com.koudesuk.functionalstorage.registry.FSAttachments;
import com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities;
import com.koudesuk.functionalstorage.registry.FunctionalStorageMenus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.network.chat.Component;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;

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
                // Framed items draw their design through this special model ("functionalstorage:framed"
                // in their items/*.json), registered before the first resource load reads them
                SpecialModelRenderers.ID_MAPPER.put(FramedDrawerItemRenderer.ID, FramedDrawerItemRenderer.Unbaked.MAP_CODEC);
                // Drawer items show what they hold on their front ("functionalstorage:drawer"), and the tools
                // color their layers by mode ("functionalstorage:tool"), like the original
                SpecialModelRenderers.ID_MAPPER.put(DrawerContentsItemRenderer.ID, DrawerContentsItemRenderer.Unbaked.MAP_CODEC);
                ItemTintSources.ID_MAPPER.put(ToolTintSource.ID, ToolTintSource.MAP_CODEC);
                HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(FunctionalStorage.MOD_ID, "fluid_drawer_hint"),
                                FluidDrawerHintHud::extractRenderState);
                ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
                        // How a data-driven size upgrade changes a drawer or a controller, like the original
                        addSizeTooltip(lines, stack.get(FSAttachments.ITEM_STORAGE_MODIFIER), "storageupgrade.obj.item_storage");
                        addSizeTooltip(lines, stack.get(FSAttachments.FLUID_STORAGE_MODIFIER), "storageupgrade.obj.fluid_storage");
                        addSizeTooltip(lines, stack.get(FSAttachments.CONTROLLER_RANGE_MODIFIER),
                                        "storageupgrade.obj.controller_range");
                        // What a functional upgrade does, like the original's ItemTooltipEvent
                        FunctionalUpgradeBehavior behavior = stack.get(FSAttachments.FUNCTIONAL_BEHAVIOR);
                        if (behavior != null)
                                lines.addAll(behavior.getTooltip());
                        // In a drawer's screen, where a direction upgrade works seen from the drawer's front
                        if (UpgradeItem.isDirectionUpgrade(stack.getItem())
                                        && Minecraft.getInstance().screen instanceof DrawerScreen screen)
                                lines.add(Math.min(3, lines.size()), Component.translatable(
                                                "drawer_upgrade.functionalstorage.relative_direction",
                                                UpgradeItem.getRelativeDirection(screen.getMenu().getTile().getFacingDirection(),
                                                                UpgradeItem.getDirection(stack)))
                                                .withStyle(ChatFormatting.YELLOW));
                });
                // Icons of what a broken drawer's item carries (DrawerBlockItem.getTooltipImage)
                ClientTooltipComponentCallback.EVENT.register(data -> data instanceof DrawerContentsTooltip contents
                                ? DrawerClientTooltipComponent.create(contents)
                                : null);
                MenuScreens.register(FunctionalStorageMenus.DRAWER, DrawerScreen::new);
                MenuScreens.register(FunctionalStorageMenus.ARMORY_CABINET,
                                com.koudesuk.functionalstorage.client.gui.ArmoryCabinetScreen::new);

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
                BlockEntityRenderers.register(FunctionalStorageBlockEntities.FRAMED_FLUID_DRAWER,
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

        private static void addSizeTooltip(java.util.List<Component> lines,
                        com.koudesuk.functionalstorage.item.component.SizeProvider provider, String object) {
                if (provider != null)
                        lines.add(provider.getTooltip(Component.translatable(object)).copy().withStyle(ChatFormatting.GRAY));
        }

        private static void registerMilkFluidRenderHandler() {
                Material still = new Material(Identifier.withDefaultNamespace("block/water_still"));
                Material flowing = new Material(Identifier.withDefaultNamespace("block/water_flow"));
                FluidModel.Unbaked milkModel = new FluidModel.Unbaked(still, flowing, null, null);
                FluidRenderingRegistry.register(FunctionalStorage.MILK, FunctionalStorage.MILK_FLOWING, milkModel);
        }
}
