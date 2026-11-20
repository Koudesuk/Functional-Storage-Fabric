package com.koudesuk.functionalstorage.network;

import com.koudesuk.functionalstorage.FunctionalStorage;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Client to server: the player left clicked a drawer front at this exact spot.
 */
public record DrawerClickPayload(BlockHitResult hit) implements FabricPacket {
    public static final PacketType<DrawerClickPayload> TYPE = PacketType.create(
            new ResourceLocation(FunctionalStorage.MOD_ID, "drawer_click"), DrawerClickPayload::new);

    public DrawerClickPayload(FriendlyByteBuf buf) {
        this(buf.readBlockHitResult());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockHitResult(hit);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
