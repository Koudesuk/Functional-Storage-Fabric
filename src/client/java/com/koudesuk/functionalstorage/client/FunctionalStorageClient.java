package com.koudesuk.functionalstorage.client;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.block.DrawerFront;
import com.koudesuk.functionalstorage.block.config.FunctionalStorageConfig;
import com.koudesuk.functionalstorage.block.tile.ControllableDrawerTile;
import com.koudesuk.functionalstorage.block.tile.FluidDrawerTile;
import com.koudesuk.functionalstorage.client.gui.DrawerScreen;
import com.koudesuk.functionalstorage.item.ConfigurationToolItem;
import com.koudesuk.functionalstorage.item.DrawerContentsTooltip;
import com.koudesuk.functionalstorage.item.LinkingToolItem;
import com.koudesuk.functionalstorage.item.UpgradeItem;
import com.koudesuk.functionalstorage.item.component.FunctionalUpgradeBehavior;
import com.koudesuk.functionalstorage.item.component.SizeProvider;
import com.koudesuk.functionalstorage.network.ConfigSyncPayload;
import com.koudesuk.functionalstorage.registry.FSAttachments;
import com.koudesuk.functionalstorage.registry.FunctionalStorageItems;
import net.fabricmc.fabric.api.event.client.player.ClientPickBlockApplyCallback;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import com.koudesuk.functionalstorage.client.model.FramedDrawerModelLoader;
import com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities;
import com.koudesuk.functionalstorage.registry.FunctionalStorageBlocks;
import com.koudesuk.functionalstorage.registry.FunctionalStorageMenus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.gui.screens.MenuScreens;

public class FunctionalStorageClient implements ClientModInitializer {
        private static boolean usingServerConfig;

        @Override
        public void onInitializeClient() {
                FunctionalStorageClientConfig.FILE.load();
                DrawerClickHandler.register();

                // Use a remote server's common config while connected to it. A singleplayer or LAN
                // host shares its config with the integrated server already.
                ClientPlayNetworking.registerGlobalReceiver(ConfigSyncPayload.TYPE, (payload, player, responseSender) -> {
                        if (!Minecraft.getInstance().hasSingleplayerServer()) {
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

                ItemTooltipCallback.EVENT.register((stack, flag, lines) -> {
                        // How a data-driven size upgrade changes a drawer or a controller, like the original
                        addSizeTooltip(lines, FSAttachments.ITEM_STORAGE_MODIFIER.get(stack), "storageupgrade.obj.item_storage");
                        addSizeTooltip(lines, FSAttachments.FLUID_STORAGE_MODIFIER.get(stack), "storageupgrade.obj.fluid_storage");
                        addSizeTooltip(lines, FSAttachments.CONTROLLER_RANGE_MODIFIER.get(stack),
                                        "storageupgrade.obj.controller_range");
                        // What a functional upgrade does, like the original's ItemTooltipEvent
                        FunctionalUpgradeBehavior behavior = FSAttachments.FUNCTIONAL_BEHAVIOR.get(stack);
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
                TooltipComponentCallback.EVENT.register(data -> data instanceof DrawerContentsTooltip contents
                                ? DrawerClientTooltipComponent.create(contents)
                                : null);
                // Which click empties or fills a held fluid container on a fluid drawer
                HudRenderCallback.EVENT.register(FluidDrawerHintHud::render);
                // Creative pick block on a drawer copies its contents, upgrades and lock, like the original
                ClientPickBlockApplyCallback.EVENT.register(FunctionalStorageClient::pickDrawer);
                registerToolColors();

                // This entrypoint is suitable for setting up client-specific logic, such as
                // rendering.
                ModelLoadingPlugin.register(new FramedDrawerModelLoader());
                MenuScreens.register(FunctionalStorageMenus.DRAWER, DrawerScreen::new);
                MenuScreens.register(FunctionalStorageMenus.ARMORY_CABINET,
                                com.koudesuk.functionalstorage.client.gui.ArmoryCabinetScreen::new);

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
                // FluidDrawerRenderer is not generic, so its provider is typed for the base tile
                BlockEntityRendererRegistry.register(FunctionalStorageBlockEntities.FRAMED_FLUID_DRAWER,
                                (BlockEntityRendererProvider<FluidDrawerTile>) ctx -> new FluidDrawerRenderer());

                // Ender drawer renderer
                BlockEntityRendererRegistry.register(FunctionalStorageBlockEntities.ENDER_DRAWER,
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
                FunctionalStorageBlocks.FRAMED_FLUID_DRAWER
                                .forEach(block -> BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.cutout()));

                // Register controller renderer for linking tool visualization
                BlockEntityRendererRegistry.register(FunctionalStorageBlockEntities.STORAGE_CONTROLLER,
                                ctx -> new ControllerRenderer<>());
                BlockEntityRendererRegistry.register(FunctionalStorageBlockEntities.FRAMED_DRAWER_CONTROLLER,
                                ctx -> new ControllerRenderer<>());

                FramedColors.register();

                registerMilkFluidRenderHandler();

                // NOTE: Dynamic item texture rendering for framed drawers is handled by
                // FramedDrawerBakedModel.emitItemQuads() when the model uses the custom loader.
                // The item models inherit from block models which use the custom loader,
                // so NBT-based textures should work when the baked model is properly resolved.
        }

        /**
         * The original enables Forge's milk fluid, which ships its own still and flowing sprites, so any mod
         * that renders a fluid can draw it. Our milk had no render handler at all: FluidRenderHandlerRegistry
         * returned null and FluidVariantRendering.getSprite() returned null for it, which crashed other mods
         * the moment they drew a milk tank or cauldron (Traveler's Backpack, Amendments through Moonlight).
         * Use the untinted water sprites, which is what our own renderers already fall back to for milk.
         */
        private static void addSizeTooltip(java.util.List<Component> lines, SizeProvider provider, String object) {
                if (provider != null)
                        lines.add(provider.getTooltip(Component.translatable(object)).copy().withStyle(ChatFormatting.GRAY));
        }

        /**
         * The original's tool colors: the Linking Tool's layers show its mode, action and stored
         * controller or frequency, the Configuration Tool's second layer its action.
         */
        private static void registerToolColors() {
                ColorProviderRegistry.ITEM.register((stack, tint) -> {
                        if (tint != 0 && stack.hasTag() && stack.getTag().contains(LinkingToolItem.NBT_ENDER))
                                return 0x2C9658;
                        if (tint == 3 && stack.hasTag() && stack.getTag().contains(LinkingToolItem.NBT_CONTROLLER))
                                return 0xFF0000;
                        if (tint == 1)
                                return LinkingToolItem.getLinkingMode(stack).getColor().getValue();
                        if (tint == 2)
                                return LinkingToolItem.getActionMode(stack).getColor().getValue();
                        return 0xFFFFFF;
                }, FunctionalStorageItems.LINKING_TOOL);
                ColorProviderRegistry.ITEM.register((stack, tint) -> tint == 1
                                ? ConfigurationToolItem.getAction(stack).getColor().getValue()
                                : 0xFFFFFF, FunctionalStorageItems.CONFIGURATION_TOOL);
        }

        /**
         * The original's Drawer.getCloneItemStack: in creative, picking a drawer gives what breaking it would
         * drop (contents, upgrades, lock), except a framed drawer with a design, which gives its style only.
         * Survival picks and Ctrl picks (vanilla adds the block entity data to those) stay vanilla.
         */
        private static ItemStack pickDrawer(Player player, HitResult result, ItemStack stack) {
                if (stack.isEmpty() || !player.getAbilities().instabuild || Screen.hasControlDown()
                                || !(result instanceof BlockHitResult hit))
                        return stack;
                if (stack.hasTag() && stack.getTag().contains("Style"))
                        return stack;
                BlockPos pos = hit.getBlockPos();
                BlockState state = player.level().getBlockState(pos);
                if (!(state.getBlock() instanceof DrawerFront drawer)
                                || !(player.level().getBlockEntity(pos) instanceof ControllableDrawerTile<?> tile))
                        return stack;
                ItemStack drop = drawer.getDropStack(state, tile);
                return drop.isEmpty() ? stack : drop;
        }

        private static void registerMilkFluidRenderHandler() {
                FluidRenderHandlerRegistry.INSTANCE.register(FunctionalStorage.MILK, FunctionalStorage.MILK_FLOWING,
                                new SimpleFluidRenderHandler(SimpleFluidRenderHandler.WATER_STILL,
                                                SimpleFluidRenderHandler.WATER_FLOWING));
        }
}
