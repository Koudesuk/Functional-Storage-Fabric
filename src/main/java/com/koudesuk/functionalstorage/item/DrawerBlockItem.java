package com.koudesuk.functionalstorage.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class DrawerBlockItem extends BlockItem {

    public DrawerBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
            Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);

        // Check standard BlockEntity data component first
        if (stack.has(DataComponents.BLOCK_ENTITY_DATA)) {
            TypedEntityData<BlockEntityType<?>> blockEntityData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
            if (blockEntityData != null) {
                CompoundTag tileTag = blockEntityData.copyTagWithoutId();
                if (tileTag.contains("Handler")) {
                    CompoundTag handlerTag = tileTag.getCompoundOrEmpty("Handler");
                    if (handlerTag.contains("BigItems")) {
                        CompoundTag bigItems = handlerTag.getCompoundOrEmpty("BigItems");
                        for (String key : bigItems.keySet()) {
                            CompoundTag itemTag = bigItems.getCompoundOrEmpty(key);
                            int amount = itemTag.getIntOr("Amount", 0);
                            // Parse item stack safely
                            ItemStack itemStack = ItemStack.CODEC.parse(
                                    net.minecraft.resources.RegistryOps.create(net.minecraft.nbt.NbtOps.INSTANCE,
                                            context.registries()),
                                    itemTag.getCompoundOrEmpty("Stack")).result().orElse(ItemStack.EMPTY);
                            if (!itemStack.isEmpty()) {
                                MutableComponent text = Component.literal("Slot " + key + ": ")
                                        .withStyle(ChatFormatting.GRAY);
                                text.append(itemStack.getDisplayName().copy().withStyle(ChatFormatting.WHITE));
                                text.append(Component.literal(" x" + amount).withStyle(ChatFormatting.GOLD));
                                tooltip.accept(text);
                            }
                        }
                    }
                }
            }
        }
    }
}
