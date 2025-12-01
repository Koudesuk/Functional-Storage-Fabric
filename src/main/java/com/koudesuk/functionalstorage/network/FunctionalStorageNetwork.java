package com.koudesuk.functionalstorage.network;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.inventory.DrawerMenu;
import com.koudesuk.functionalstorage.item.UpgradeItem;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class FunctionalStorageNetwork {
    public static final ResourceLocation UPDATE_UPGRADE_DIRECTION = new ResourceLocation(FunctionalStorage.MOD_ID, "update_upgrade_direction");

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(UPDATE_UPGRADE_DIRECTION, (server, player, handler, buf, responseSender) -> {
            int slotIndex = buf.readInt();
            server.execute(() -> {
                if (player.containerMenu instanceof DrawerMenu menu) {
                    if (slotIndex >= 0 && slotIndex < menu.slots.size()) {
                        ItemStack stack = menu.getSlot(slotIndex).getItem();
                        if (stack.getItem() instanceof UpgradeItem) {
                            CompoundTag tag = stack.getOrCreateTag();
                            Direction direction = UpgradeItem.getDirection(stack);
                            Direction next = Direction.values()[(direction.ordinal() + 1) % Direction.values().length];
                            tag.putString("Direction", next.getName());
                            stack.setTag(tag);
                        }
                    }
                }
            });
        });
    }
}
