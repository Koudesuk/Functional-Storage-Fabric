package com.koudesuk.functionalstorage.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DrawerBlockItem extends BlockItem {

    public DrawerBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        // Check standard BlockEntity data component first
        if (stack.has(DataComponents.BLOCK_ENTITY_DATA)) {
            CustomData customData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
            if (customData != null) {
                CompoundTag tileTag = customData.copyTag();
                if (tileTag.contains("Handler")) {
                    CompoundTag handlerTag = tileTag.getCompound("Handler");
                    if (handlerTag.contains("BigItems")) {
                        CompoundTag bigItems = handlerTag.getCompound("BigItems");
                        for (String key : bigItems.getAllKeys()) {
                            CompoundTag itemTag = bigItems.getCompound(key);
                            int amount = itemTag.getInt("Amount");
                            // Parse item stack safely
                            ItemStack itemStack = ItemStack.CODEC.parse(
                                    net.minecraft.resources.RegistryOps.create(net.minecraft.nbt.NbtOps.INSTANCE,
                                            context.registries()),
                                    itemTag.getCompound("Stack")).result().orElse(ItemStack.EMPTY);
                            if (!itemStack.isEmpty()) {
                                MutableComponent text = Component.literal("Slot " + key + ": ")
                                        .withStyle(ChatFormatting.GRAY);
                                text.append(itemStack.getDisplayName().copy().withStyle(ChatFormatting.WHITE));
                                text.append(Component.literal(" x" + amount).withStyle(ChatFormatting.GOLD));
                                tooltip.add(text);
                            }
                        }
                    }
                }
            }
        }
    }
}
