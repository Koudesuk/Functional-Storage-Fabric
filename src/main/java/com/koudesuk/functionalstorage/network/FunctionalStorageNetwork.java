package com.koudesuk.functionalstorage.network;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.block.DrawerFront;
import com.koudesuk.functionalstorage.block.config.FunctionalStorageConfig;
import com.koudesuk.functionalstorage.block.tile.ControllableDrawerTile;
import net.minecraft.core.BlockPos;
import com.koudesuk.functionalstorage.inventory.DrawerMenu;
import com.koudesuk.functionalstorage.item.UpgradeItem;
import com.koudesuk.functionalstorage.registry.FSAttachments;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class FunctionalStorageNetwork {

    public static void register() {
        PayloadTypeRegistry.playC2S().register(DrawerClickPayload.TYPE, DrawerClickPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(ConfigSyncPayload.TYPE, ConfigSyncPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(UpdateUpgradeDirectionPayload.ID,
                UpdateUpgradeDirectionPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(DrawerPriorityPayload.TYPE, DrawerPriorityPayload.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(DrawerClickPayload.TYPE,
                (payload, context) -> DrawerFront.click(context.player(), payload.hit()));

        // Same checks as the original's DrawerPriorityMessage: within 8 blocks, never below 0
        ServerPlayNetworking.registerGlobalReceiver(DrawerPriorityPayload.TYPE, (payload, context) -> {
            BlockPos pos = payload.pos();
            if (context.player().distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 64D)
                return;
            if (context.player().level().getBlockEntity(pos) instanceof ControllableDrawerTile<?> drawer)
                drawer.setPriority(Math.max(0, payload.priority()));
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (ServerPlayNetworking.canSend(handler, ConfigSyncPayload.TYPE))
                sender.sendPacket(new ConfigSyncPayload(FunctionalStorageConfig.FILE.snapshot()));
        });

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
