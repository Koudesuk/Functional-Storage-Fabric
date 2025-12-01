package com.koudesuk.functionalstorage.block;

import com.koudesuk.functionalstorage.block.tile.FramedDrawerTile;
import com.koudesuk.functionalstorage.client.model.FramedDrawerModelData;
import com.koudesuk.functionalstorage.util.DrawerType;
import com.koudesuk.functionalstorage.util.DrawerWoodType;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

public class FramedDrawerBlock extends DrawerBlock {

    public FramedDrawerBlock(DrawerType type) {
        super(DrawerWoodType.FRAMED, type,
                FabricBlockSettings.copyOf(Blocks.OAK_PLANKS).strength(1.5f, 1.5f).noOcclusion());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FramedDrawerTile(pos, state, this.getType());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof FramedDrawerTile framedDrawerTile) {
            framedDrawerTile.setFramedDrawerModelData(getDrawerModelData(stack));
        }
    }

    public static FramedDrawerModelData getDrawerModelData(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("Style")) {
            CompoundTag tag = stack.getTag().getCompound("Style");
            if (tag.isEmpty())
                return null;
            HashMap<String, Item> data = new HashMap<>();
            data.put("particle", BuiltInRegistries.ITEM.get(new ResourceLocation(tag.getString("particle"))));
            data.put("front", BuiltInRegistries.ITEM.get(new ResourceLocation(tag.getString("front"))));
            data.put("side", BuiltInRegistries.ITEM.get(new ResourceLocation(tag.getString("side"))));
            data.put("front_divider", BuiltInRegistries.ITEM.get(new ResourceLocation(tag.getString("front_divider"))));
            return new FramedDrawerModelData(data);
        }
        return null;
    }

    public static ItemStack fill(ItemStack first, ItemStack second, ItemStack drawer, ItemStack divider) {
        drawer = drawer.copy();
        drawer.setCount(1);
        CompoundTag style = drawer.getOrCreateTagElement("Style");
        style.putString("particle", BuiltInRegistries.ITEM.getKey(first.getItem()).toString());
        style.putString("side", BuiltInRegistries.ITEM.getKey(first.getItem()).toString());
        style.putString("front", BuiltInRegistries.ITEM.getKey(second.getItem()).toString());
        if (divider.isEmpty()) {
            style.putString("front_divider", BuiltInRegistries.ITEM.getKey(first.getItem()).toString());
        } else {
            style.putString("front_divider", BuiltInRegistries.ITEM.getKey(divider.getItem()).toString());
        }
        drawer.getOrCreateTag().put("Style", style);
        return drawer;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        NonNullList<ItemStack> stacks = NonNullList.create();
        ItemStack stack = new ItemStack(this);
        BlockEntity drawerTile = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (drawerTile instanceof FramedDrawerTile framedDrawerTile) {
            if (!framedDrawerTile.isEverythingEmpty()) {
                stack.getOrCreateTag().put("Tile", drawerTile.saveWithoutMetadata());
            }
            if (framedDrawerTile.getFramedDrawerModelData() != null) {
                stack.getOrCreateTag().put("Style", framedDrawerTile.getFramedDrawerModelData().serializeNBT());
            }
            if (framedDrawerTile.isLocked()) {
                stack.getOrCreateTag().putBoolean("Locked", framedDrawerTile.isLocked());
            }
        }
        stacks.add(stack);
        return stacks;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof FramedDrawerTile framedDrawerTile && framedDrawerTile.getFramedDrawerModelData() != null
                && !framedDrawerTile.getFramedDrawerModelData().getDesign().isEmpty()) {
            ItemStack stack = new ItemStack(this);
            stack.getOrCreateTag().put("Style", framedDrawerTile.getFramedDrawerModelData().serializeNBT());
            return stack;
        }
        return super.getCloneItemStack(level, pos, state);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
            TooltipFlag flag) {
        tooltip.add(Component.translatable("frameddrawer.use.line1").withStyle(net.minecraft.ChatFormatting.GRAY));
        tooltip.add(Component.translatable("frameddrawer.use.line2").withStyle(net.minecraft.ChatFormatting.GRAY));
        tooltip.add(Component.translatable("frameddrawer.use.line3").withStyle(net.minecraft.ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
