package com.koudesuk.functionalstorage.network;

import com.koudesuk.functionalstorage.FunctionalStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record UpdateUpgradeDirectionPayload(int slot, int direction) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdateUpgradeDirectionPayload> ID = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(FunctionalStorage.MOD_ID, "update_upgrade_direction"));

    public static final StreamCodec<FriendlyByteBuf, UpdateUpgradeDirectionPayload> STREAM_CODEC = StreamCodec
            .composite(
                    net.minecraft.network.codec.ByteBufCodecs.INT, UpdateUpgradeDirectionPayload::slot,
                    net.minecraft.network.codec.ByteBufCodecs.INT, UpdateUpgradeDirectionPayload::direction,
                    UpdateUpgradeDirectionPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
