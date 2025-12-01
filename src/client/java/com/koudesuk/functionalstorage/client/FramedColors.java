package com.koudesuk.functionalstorage.client;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.block.FramedDrawerBlock;
import com.koudesuk.functionalstorage.block.tile.FramedCompactingDrawerTile;
import com.koudesuk.functionalstorage.block.tile.FramedDrawerControllerTile;
import com.koudesuk.functionalstorage.block.tile.FramedDrawerTile;
import com.koudesuk.functionalstorage.block.tile.FramedSimpleCompactingDrawerTile;
import com.koudesuk.functionalstorage.client.model.FramedDrawerModelData;
import com.koudesuk.functionalstorage.registry.FunctionalStorageBlocks;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FramedColors {

    public static void register() {
        List<net.minecraft.world.level.block.Block> framedBlocks = new ArrayList<>();
        framedBlocks.addAll(FunctionalStorageBlocks.FRAMED_DRAWER);
        framedBlocks.add(FunctionalStorageBlocks.FRAMED_COMPACTING_DRAWER);
        framedBlocks.add(FunctionalStorageBlocks.FRAMED_SIMPLE_COMPACTING_DRAWER);
        framedBlocks.add(FunctionalStorageBlocks.FRAMED_DRAWER_CONTROLLER);

        ColorProviderRegistry.BLOCK.register(FramedColors::getBlockColor,
                framedBlocks.toArray(new net.minecraft.world.level.block.Block[0]));

        ColorProviderRegistry.ITEM.register(FramedColors::getItemColor,
                framedBlocks.stream().map(net.minecraft.world.level.block.Block::asItem).toArray(Item[]::new));
    }

    private static int getBlockColor(BlockState state, BlockAndTintGetter world, BlockPos pos, int tintIndex) {
        if (world != null && pos != null && tintIndex == 0) {
            BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof FramedDrawerTile tile) {
                return resolveColor(tile.getFramedDrawerModelData(), world, pos, tintIndex);
            }
            if (entity instanceof FramedCompactingDrawerTile tile) {
                return resolveColor(tile.getFramedDrawerModelData(), world, pos, tintIndex);
            }
            if (entity instanceof FramedSimpleCompactingDrawerTile tile) {
                return resolveColor(tile.getFramedDrawerModelData(), world, pos, tintIndex);
            }
            if (entity instanceof FramedDrawerControllerTile tile) {
                return resolveColor(tile.getFramedDrawerModelData(), world, pos, tintIndex);
            }
        }
        return 0xFFFFFF;
    }

    private static int getItemColor(ItemStack stack, int tintIndex) {
        if (tintIndex == 0 && stack.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof FramedDrawerBlock) {
            FramedDrawerModelData data = FramedDrawerBlock.getDrawerModelData(stack);
            if (data != null) {
                return resolveItemColor(data, tintIndex);
            }
        }
        return 0xFFFFFF;
    }

    private static int resolveColor(FramedDrawerModelData data, BlockAndTintGetter world, BlockPos pos, int tintIndex) {
        if (data == null)
            return 0xFFFFFF;
        for (Map.Entry<String, Item> entry : data.getDesign().entrySet()) {
            if (entry.getValue() instanceof BlockItem item) {
                ResourceLocation id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);
                if (id != null && id.getNamespace().equals(FunctionalStorage.MOD_ID)) {
                    continue; // skip framed placeholder blocks to avoid self-reference
                }
                BlockState other = item.getBlock().defaultBlockState();
                int color = Minecraft.getInstance().getBlockColors().getColor(other, world, pos, tintIndex);
                if (color != -1) {
                    return color;
                }
            }
        }
        return 0xFFFFFF;
    }

    private static int resolveItemColor(FramedDrawerModelData data, int tintIndex) {
        if (data == null)
            return 0xFFFFFF;
        for (Map.Entry<String, Item> entry : data.getDesign().entrySet()) {
            if (entry.getValue() instanceof BlockItem item) {
                var blockState = item.getBlock().defaultBlockState();
                int color = Minecraft.getInstance().getBlockColors().getColor(blockState, null, null, tintIndex);
                if (color != -1) {
                    return color;
                }
            }
        }
        return 0xFFFFFF;
    }
}
