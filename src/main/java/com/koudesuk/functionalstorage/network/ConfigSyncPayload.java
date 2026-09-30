package com.koudesuk.functionalstorage.network;

import com.koudesuk.functionalstorage.FunctionalStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

/**
 * Server to client: the server's common config, so capacities and ranges shown on the
 * client match what the server actually enforces.
 */
public record ConfigSyncPayload(Map<String, String> values) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ConfigSyncPayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(FunctionalStorage.MOD_ID, "config_sync"));

    public static final StreamCodec<FriendlyByteBuf, ConfigSyncPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeMap(payload.values(), FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeUtf),
            buf -> new ConfigSyncPayload(buf.readMap(FriendlyByteBuf::readUtf, FriendlyByteBuf::readUtf)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
