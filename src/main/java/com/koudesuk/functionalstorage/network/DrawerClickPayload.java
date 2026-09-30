package com.koudesuk.functionalstorage.network;

import com.koudesuk.functionalstorage.FunctionalStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Client to server: the player left clicked a drawer front at this exact spot.
 */
public record DrawerClickPayload(BlockHitResult hit) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DrawerClickPayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(FunctionalStorage.MOD_ID, "drawer_click"));

    public static final StreamCodec<FriendlyByteBuf, DrawerClickPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeBlockHitResult(payload.hit()),
            buf -> new DrawerClickPayload(buf.readBlockHitResult()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
