package com.koudesuk.functionalstorage.network;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.inventory.DrawerMenu;
import com.koudesuk.functionalstorage.item.UpgradeItem;
import com.koudesuk.functionalstorage.registry.FSAttachments;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class FunctionalStorageNetwork {

    public static void register() {
        PayloadTypeRegistry.playC2S().register(UpdateUpgradeDirectionPayload.ID,
                UpdateUpgradeDirectionPayload.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(UpdateUpgradeDirectionPayload.ID,
                (payload, context) -> {
                    context.server().execute(() -> {
                        if (context.player().containerMenu instanceof DrawerMenu menu) {
                            int slotIndex = payload.slot();
                            if (slotIndex >= 0 && slotIndex < menu.slots.size()) {
                                ItemStack stack = menu.getSlot(slotIndex).getItem();
                                if (stack.getItem() instanceof UpgradeItem) {
                                    Direction direction = UpgradeItem.getDirection(stack);
                                    Direction next = Direction.values()[(direction.ordinal() + 1)
                                            % Direction.values().length];
                                    stack.set(FSAttachments.DIRECTION, next);
                                }
                            }
                        }
                    });
                });
    }
}
