package com.koudesuk.functionalstorage.block;

import com.koudesuk.functionalstorage.block.tile.FramedDrawerTile;
import com.koudesuk.functionalstorage.client.model.FramedDrawerModelData;
import com.koudesuk.functionalstorage.registry.FSAttachments;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

public class FramedDrawerBlock extends DrawerBlock {

    public FramedDrawerBlock(DrawerType type) {
        super(DrawerWoodType.FRAMED, type,
                FabricBlockSettings.copyOf(Blocks.OAK_PLANKS).strength(1.5f, 1.5f).noOcclusion()
                        .isViewBlocking((state, level, pos) -> false));
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
        // Check FSAttachments.STYLE first (new 1.21 API)
        if (stack.has(FSAttachments.STYLE)) {
            CompoundTag styleTag = stack.get(FSAttachments.STYLE);
            if (styleTag != null && !styleTag.isEmpty()) {
                HashMap<String, Item> data = new HashMap<>();
                data.put("particle",
                        BuiltInRegistries.ITEM.get(ResourceLocation.parse(styleTag.getString("particle"))));
                data.put("front", BuiltInRegistries.ITEM.get(ResourceLocation.parse(styleTag.getString("front"))));
                data.put("side", BuiltInRegistries.ITEM.get(ResourceLocation.parse(styleTag.getString("side"))));
                data.put("front_divider",
                        BuiltInRegistries.ITEM.get(ResourceLocation.parse(styleTag.getString("front_divider"))));
                return new FramedDrawerModelData(data);
            }
        }
        return null;
    }

    public static ItemStack fill(ItemStack first, ItemStack second, ItemStack drawer, ItemStack divider) {
        drawer = drawer.copy();
        drawer.setCount(1);
        CompoundTag style = new CompoundTag();
        style.putString("particle", BuiltInRegistries.ITEM.getKey(first.getItem()).toString());
        style.putString("side", BuiltInRegistries.ITEM.getKey(first.getItem()).toString());
        style.putString("front", BuiltInRegistries.ITEM.getKey(second.getItem()).toString());
        if (divider.isEmpty()) {
            style.putString("front_divider", BuiltInRegistries.ITEM.getKey(first.getItem()).toString());
        } else {
            style.putString("front_divider", BuiltInRegistries.ITEM.getKey(divider.getItem()).toString());
        }
        drawer.set(FSAttachments.STYLE, style);
        return drawer;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        NonNullList<ItemStack> stacks = NonNullList.create();
        ItemStack stack = new ItemStack(this);
        BlockEntity drawerTile = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (drawerTile instanceof FramedDrawerTile framedDrawerTile) {
            if (!framedDrawerTile.isEverythingEmpty() && framedDrawerTile.getLevel() != null) {
                stack.set(FSAttachments.TILE,
                        com.koudesuk.functionalstorage.util.ItemStackHelper.saveBlockEntityData(framedDrawerTile));
            }
            if (framedDrawerTile.getFramedDrawerModelData() != null && framedDrawerTile.getLevel() != null) {
                stack.set(FSAttachments.STYLE, framedDrawerTile.getFramedDrawerModelData()
                        .serializeNBT(framedDrawerTile.getLevel().registryAccess()));
            }
            if (framedDrawerTile.isLocked()) {
                stack.set(FSAttachments.LOCKED, framedDrawerTile.isLocked());
            }
        }
        stacks.add(stack);
        return stacks;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof FramedDrawerTile framedDrawerTile && framedDrawerTile.getFramedDrawerModelData() != null
                && !framedDrawerTile.getFramedDrawerModelData().getDesign().isEmpty()) {
            ItemStack stack = new ItemStack(this);
            stack.set(FSAttachments.STYLE,
                    framedDrawerTile.getFramedDrawerModelData().serializeNBT(level.registryAccess()));
            return stack;
        }
        return new ItemStack(this);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
            TooltipFlag flag) {
        // Parse newlines in frameddrawer.use and add each line as a separate Component
        String useText = Component.translatable("frameddrawer.use").getString();
        for (String line : useText.split("\n")) {
            if (!line.trim().isEmpty()) {
                tooltip.add(Component.literal(line.trim()).withStyle(net.minecraft.ChatFormatting.GRAY));
            }
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
