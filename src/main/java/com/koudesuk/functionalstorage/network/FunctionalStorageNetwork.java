package com.koudesuk.functionalstorage.network;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.block.DrawerFront;
import com.koudesuk.functionalstorage.block.config.FunctionalStorageConfig;
import com.koudesuk.functionalstorage.block.tile.ControllableDrawerTile;
import com.koudesuk.functionalstorage.inventory.DrawerMenu;
import com.koudesuk.functionalstorage.item.UpgradeItem;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
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

        // Left clicks on drawer fronts, see DrawerClickHandler (handlers of FabricPackets run on the server thread)
        ServerPlayNetworking.registerGlobalReceiver(DrawerClickPayload.TYPE,
                (payload, player, responseSender) -> DrawerFront.click(player, payload.hit()));

        // Same checks as the original's DrawerPriorityMessage: within 8 blocks, never below 0
        ServerPlayNetworking.registerGlobalReceiver(DrawerPriorityPayload.TYPE, (payload, player, responseSender) -> {
            BlockPos pos = payload.pos();
            if (player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > 64D)
                return;
            if (player.level().getBlockEntity(pos) instanceof ControllableDrawerTile<?> drawer)
                drawer.setPriority(Math.max(0, payload.priority()));
        });

        // The armory cabinet screen's search and scroll row
        ServerPlayNetworking.registerGlobalReceiver(ArmorySearchPayload.TYPE, (payload, player, responseSender) -> {
            if (player.containerMenu instanceof com.koudesuk.functionalstorage.inventory.ArmoryCabinetMenu menu
                    && menu.containerId == payload.containerId())
                menu.applySearch(payload.query(), payload.scrollRow());
        });

        // The server's common config, so what clients show matches what the server enforces
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (ServerPlayNetworking.canSend(handler, ConfigSyncPayload.TYPE))
                sender.sendPacket(new ConfigSyncPayload(FunctionalStorageConfig.FILE.snapshot()));
        });
    }
}
