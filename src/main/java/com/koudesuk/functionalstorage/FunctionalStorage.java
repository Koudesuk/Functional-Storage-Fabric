package com.koudesuk.functionalstorage;

import com.koudesuk.functionalstorage.fluid.MilkFluid;
import com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities;
import com.koudesuk.functionalstorage.registry.FunctionalStorageBlocks;
import com.koudesuk.functionalstorage.registry.FunctionalStorageItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.EmptyItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.FullItemFluidStorage;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FunctionalStorage implements ModInitializer {
        public static final String MOD_ID = "functionalstorage";
        public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

        // Milk fluid - equivalent to Forge's ForgeMod.enableMilkFluid()
        public static final Fluid MILK = new MilkFluid.Still();
        public static final Fluid MILK_FLOWING = new MilkFluid.Flowing();

        @Override
        public void onInitialize() {
                LOGGER.info("Functional Storage initializing...");

                // Register milk fluid (equivalent to ForgeMod.enableMilkFluid())
                Registry.register(BuiltInRegistries.FLUID, new ResourceLocation(MOD_ID, "milk"), MILK);
                Registry.register(BuiltInRegistries.FLUID, new ResourceLocation(MOD_ID, "milk_flowing"), MILK_FLOWING);

                // Register milk fluid attribute handler for proper display name
                FluidVariantAttributes.register(MILK, new FluidVariantAttributeHandler() {
                        @Override
                        public Component getName(FluidVariant fluidVariant) {
                                return Component.translatable("fluid.functionalstorage.milk");
                        }
                });
                FluidVariantAttributes.register(MILK_FLOWING, new FluidVariantAttributeHandler() {
                        @Override
                        public Component getName(FluidVariant fluidVariant) {
                                return Component.translatable("fluid.functionalstorage.milk");
                        }
                });

                // Register milk bucket as a fluid container (full -> empty)
                FluidStorage.combinedItemApiProvider(Items.MILK_BUCKET).register(ctx -> new FullItemFluidStorage(ctx,
                                Items.BUCKET, FluidVariant.of(MILK), FluidConstants.BUCKET));

                // Register empty bucket to accept fluids (enables extraction from drawers)
                // Water bucket
                FluidStorage.combinedItemApiProvider(Items.BUCKET).register(ctx -> new EmptyItemFluidStorage(ctx,
                                Items.WATER_BUCKET, Fluids.WATER, FluidConstants.BUCKET));
                // Lava bucket
                FluidStorage.combinedItemApiProvider(Items.BUCKET).register(ctx -> new EmptyItemFluidStorage(ctx,
                                Items.LAVA_BUCKET, Fluids.LAVA, FluidConstants.BUCKET));
                // Milk bucket
                FluidStorage.combinedItemApiProvider(Items.BUCKET).register(
                                ctx -> new EmptyItemFluidStorage(ctx, Items.MILK_BUCKET, MILK, FluidConstants.BUCKET));

                LOGGER.info("Registered fluid handlers for bucket interactions");

                FunctionalStorageBlocks.register();
                com.koudesuk.functionalstorage.registry.FunctionalStorageGroup.register();
                com.koudesuk.functionalstorage.registry.FunctionalStorageRecipes.register();
                FunctionalStorageBlockEntities.register();
                FunctionalStorageItems.register();
                com.koudesuk.functionalstorage.registry.FunctionalStorageMenus.register();

                net.fabricmc.fabric.api.transfer.v1.item.ItemStorage.SIDED.registerForBlockEntity(
                                (drawer, direction) -> drawer.getHandler(),
                                com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities.DRAWER);
                net.fabricmc.fabric.api.transfer.v1.item.ItemStorage.SIDED.registerForBlockEntity(
                                (cabinet, direction) -> cabinet.handler,
                                com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities.ARMORY_CABINET);
                net.fabricmc.fabric.api.transfer.v1.item.ItemStorage.SIDED.registerForBlockEntity(
                                (extension, direction) -> extension.getStorage(),
                                com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities.CONTROLLER_EXTENSION);
                net.fabricmc.fabric.api.transfer.v1.item.ItemStorage.SIDED.registerForBlockEntity(
                                (drawer, direction) -> drawer.getStorage(),
                                com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities.SIMPLE_COMPACTING_DRAWER);
                net.fabricmc.fabric.api.transfer.v1.item.ItemStorage.SIDED.registerForBlockEntity(
                                (drawer, direction) -> drawer.getStorage(),
                                com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities.FRAMED_DRAWER);
                net.fabricmc.fabric.api.transfer.v1.item.ItemStorage.SIDED.registerForBlockEntity(
                                (controller, direction) -> controller.getStorage(),
                                com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities.STORAGE_CONTROLLER);
                net.fabricmc.fabric.api.transfer.v1.item.ItemStorage.SIDED.registerForBlockEntity(
                                (controller, direction) -> controller.getStorage(),
                                com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities.FRAMED_DRAWER_CONTROLLER);
                net.fabricmc.fabric.api.transfer.v1.item.ItemStorage.SIDED.registerForBlockEntity(
                                (drawer, direction) -> drawer.getStorage(),
                                com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities.COMPACTING_DRAWER);
                net.fabricmc.fabric.api.transfer.v1.item.ItemStorage.SIDED.registerForBlockEntity(
                                (drawer, direction) -> drawer.getStorage(),
                                com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities.FRAMED_COMPACTING_DRAWER);
                net.fabricmc.fabric.api.transfer.v1.item.ItemStorage.SIDED.registerForBlockEntity(
                                (drawer, direction) -> drawer.getStorage(),
                                com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities.FRAMED_SIMPLE_COMPACTING_DRAWER);
                net.fabricmc.fabric.api.transfer.v1.item.ItemStorage.SIDED.registerForBlockEntity(
                                (drawer, direction) -> drawer.getStorage(),
                                com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities.ENDER_DRAWER);

                // Register FluidStorage for Fluid Drawers
                net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage.SIDED.registerForBlockEntity(
                                (drawer, direction) -> drawer.getHandler(),
                                com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities.FLUID_DRAWER_1);
                net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage.SIDED.registerForBlockEntity(
                                (drawer, direction) -> drawer.getHandler(),
                                com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities.FLUID_DRAWER_2);
                net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage.SIDED.registerForBlockEntity(
                                (drawer, direction) -> drawer.getHandler(),
                                com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities.FLUID_DRAWER_4);

                // Register FluidStorage for Storage Controllers (enables AE2 and other mods to
                // access fluid drawers via controller)
                net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage.SIDED.registerForBlockEntity(
                                (controller, direction) -> controller.getFluidHandler(),
                                com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities.STORAGE_CONTROLLER);
                net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage.SIDED.registerForBlockEntity(
                                (controller, direction) -> controller.getFluidHandler(),
                                com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities.FRAMED_DRAWER_CONTROLLER);

                // Handle left-click (attack) on drawer blocks to extract items
                net.fabricmc.fabric.api.event.player.AttackBlockCallback.EVENT
                                .register((player, world, hand, pos, direction) -> {
                                        net.minecraft.world.level.block.state.BlockState state = world
                                                        .getBlockState(pos);
                                        net.minecraft.world.phys.HitResult result = player.pick(20, 0, false);
                                        if (!(result instanceof net.minecraft.world.phys.BlockHitResult blockHitResult)) {
                                                return net.minecraft.world.InteractionResult.PASS;
                                        }
                                        if (!blockHitResult.getBlockPos().equals(pos)) {
                                                return net.minecraft.world.InteractionResult.PASS;
                                        }

                                        // Handle LinkingTool left-click on EnderDrawer to store frequency
                                        net.minecraft.world.item.ItemStack heldItem = player.getItemInHand(hand);
                                        if (heldItem.getItem() instanceof com.koudesuk.functionalstorage.item.LinkingToolItem) {
                                                net.minecraft.world.level.block.entity.BlockEntity blockEntity = world
                                                                .getBlockEntity(pos);
                                                if (blockEntity instanceof com.koudesuk.functionalstorage.block.tile.EnderDrawerTile enderTile) {
                                                        if (!world.isClientSide()) {
                                                                heldItem.getOrCreateTag().putString(
                                                                                com.koudesuk.functionalstorage.item.LinkingToolItem.NBT_ENDER,
                                                                                enderTile.getFrequency());
                                                                player.displayClientMessage(
                                                                                net.minecraft.network.chat.Component
                                                                                                .translatable("linkingtool.ender.stored")
                                                                                                .setStyle(net.minecraft.network.chat.Style.EMPTY
                                                                                                                .withColor(
                                                                                                                                com.koudesuk.functionalstorage.item.LinkingToolItem.LinkingMode.SINGLE
                                                                                                                                                .getColor())),
                                                                                true);
                                                        }
                                                        return net.minecraft.world.InteractionResult.SUCCESS;
                                                }
                                        }

                                        // Handle DrawerBlock
                                        if (state.getBlock() instanceof com.koudesuk.functionalstorage.block.DrawerBlock drawerBlock) {
                                                int hit = drawerBlock.getHit(state, pos, blockHitResult);
                                                if (hit != -1) {
                                                        if (!world.isClientSide()) {
                                                                net.minecraft.world.level.block.entity.BlockEntity blockEntity = world
                                                                                .getBlockEntity(pos);
                                                                if (blockEntity instanceof com.koudesuk.functionalstorage.block.tile.DrawerTile drawerTile) {
                                                                        drawerTile.onClicked(player, hit);
                                                                }
                                                        }
                                                        return net.minecraft.world.InteractionResult.SUCCESS;
                                                }
                                        }

                                        // Handle CompactingDrawerBlock (including framed)
                                        if (state.getBlock() instanceof com.koudesuk.functionalstorage.block.CompactingDrawerBlock compactingBlock) {
                                                int hit = compactingBlock.getHit(state, pos, blockHitResult);
                                                if (hit != -1) {
                                                        if (!world.isClientSide()) {
                                                                net.minecraft.world.level.block.entity.BlockEntity blockEntity = world
                                                                                .getBlockEntity(pos);
                                                                if (blockEntity instanceof com.koudesuk.functionalstorage.block.tile.CompactingDrawerTile compactingTile) {
                                                                        compactingTile.onClicked(player, hit);
                                                                }
                                                        }
                                                        return net.minecraft.world.InteractionResult.SUCCESS;
                                                }
                                        }

                                        // Handle SimpleCompactingDrawerBlock (including framed)
                                        if (state.getBlock() instanceof com.koudesuk.functionalstorage.block.SimpleCompactingDrawerBlock simpleCompactingBlock) {
                                                int hit = simpleCompactingBlock.getHit(state, pos, blockHitResult);
                                                if (hit != -1) {
                                                        if (!world.isClientSide()) {
                                                                net.minecraft.world.level.block.entity.BlockEntity blockEntity = world
                                                                                .getBlockEntity(pos);
                                                                if (blockEntity instanceof com.koudesuk.functionalstorage.block.tile.SimpleCompactingDrawerTile simpleCompactingTile) {
                                                                        simpleCompactingTile.onClicked(player, hit);
                                                                }
                                                        }
                                                        return net.minecraft.world.InteractionResult.SUCCESS;
                                                }
                                        }

                                        // Handle FluidDrawerBlock
                                        if (state.getBlock() instanceof com.koudesuk.functionalstorage.block.FluidDrawerBlock fluidBlock) {
                                                int hit = fluidBlock.getHit(state, pos, blockHitResult);
                                                if (hit != -1) {
                                                        if (!world.isClientSide()) {
                                                                net.minecraft.world.level.block.entity.BlockEntity blockEntity = world
                                                                                .getBlockEntity(pos);
                                                                if (blockEntity instanceof com.koudesuk.functionalstorage.block.tile.FluidDrawerTile fluidTile) {
                                                                        fluidTile.onClicked(player, hit);
                                                                }
                                                        }
                                                        return net.minecraft.world.InteractionResult.SUCCESS;
                                                }
                                        }

                                        // Handle EnderDrawerBlock
                                        if (state.getBlock() instanceof com.koudesuk.functionalstorage.block.EnderDrawerBlock enderBlock) {
                                                int hit = enderBlock.getHit(state, pos, blockHitResult);
                                                if (hit != -1) {
                                                        if (!world.isClientSide()) {
                                                                net.minecraft.world.level.block.entity.BlockEntity blockEntity = world
                                                                                .getBlockEntity(pos);
                                                                if (blockEntity instanceof com.koudesuk.functionalstorage.block.tile.EnderDrawerTile enderTile) {
                                                                        enderTile.onClicked(player, hit);
                                                                }
                                                        }
                                                        return net.minecraft.world.InteractionResult.SUCCESS;
                                                }
                                        }

                                        return net.minecraft.world.InteractionResult.PASS;
                                });

                // Prevent block breaking when clicking on drawer front face (especially for
                // creative mode)
                net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.BEFORE
                                .register((world, player, pos, state, blockEntity) -> {
                                        // Only check in creative mode - survival mode uses getDestroyProgress
                                        if (!player.isCreative()) {
                                                return true; // Allow normal breaking in survival
                                        }

                                        net.minecraft.world.phys.HitResult result = player.pick(20, 0, false);
                                        if (!(result instanceof net.minecraft.world.phys.BlockHitResult blockHitResult)) {
                                                return true; // Allow breaking if no hit result
                                        }
                                        if (!blockHitResult.getBlockPos().equals(pos)) {
                                                return true; // Allow breaking if hit result doesn't match
                                        }

                                        // Check each drawer type and cancel if hitting front face
                                        if (state.getBlock() instanceof com.koudesuk.functionalstorage.block.DrawerBlock drawerBlock) {
                                                int hit = drawerBlock.getHit(state, pos, blockHitResult);
                                                if (hit != -1) {
                                                        return false; // Cancel breaking - front face was clicked
                                                }
                                        }
                                        if (state.getBlock() instanceof com.koudesuk.functionalstorage.block.CompactingDrawerBlock compactingBlock) {
                                                int hit = compactingBlock.getHit(state, pos, blockHitResult);
                                                if (hit != -1) {
                                                        return false; // Cancel breaking - front face was clicked
                                                }
                                        }
                                        if (state.getBlock() instanceof com.koudesuk.functionalstorage.block.SimpleCompactingDrawerBlock simpleCompactingBlock) {
                                                int hit = simpleCompactingBlock.getHit(state, pos, blockHitResult);
                                                if (hit != -1) {
                                                        return false; // Cancel breaking - front face was clicked
                                                }
                                        }
                                        if (state.getBlock() instanceof com.koudesuk.functionalstorage.block.FluidDrawerBlock fluidBlock) {
                                                int hit = fluidBlock.getHit(state, pos, blockHitResult);
                                                if (hit != -1) {
                                                        return false; // Cancel breaking - front face was clicked
                                                }
                                        }
                                        if (state.getBlock() instanceof com.koudesuk.functionalstorage.block.EnderDrawerBlock enderBlock) {
                                                int hit = enderBlock.getHit(state, pos, blockHitResult);
                                                if (hit != -1) {
                                                        return false; // Cancel breaking - front face was clicked
                                                }
                                        }

                                        return true; // Allow breaking for all other cases
                                });
        }
}
