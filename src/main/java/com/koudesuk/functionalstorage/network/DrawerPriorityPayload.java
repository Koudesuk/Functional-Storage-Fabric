package com.koudesuk.functionalstorage.network;

import com.koudesuk.functionalstorage.FunctionalStorage;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * Client to server: the priority typed into a drawer's screen, like the original's DrawerPriorityMessage.
 */
public record DrawerPriorityPayload(BlockPos pos, int priority) implements FabricPacket {
    public static final PacketType<DrawerPriorityPayload> TYPE = PacketType.create(
            new ResourceLocation(FunctionalStorage.MOD_ID, "drawer_priority"), DrawerPriorityPayload::new);

    public DrawerPriorityPayload(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), buf.readVarInt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeVarInt(priority);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
