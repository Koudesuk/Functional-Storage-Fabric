package com.koudesuk.functionalstorage.network;

import com.koudesuk.functionalstorage.FunctionalStorage;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

/**
 * Server to client: the server's common config, so capacities and ranges shown on the
 * client match what the server actually enforces.
 */
public record ConfigSyncPayload(Map<String, String> values) implements FabricPacket {
    public static final PacketType<ConfigSyncPayload> TYPE = PacketType.create(
            new ResourceLocation(FunctionalStorage.MOD_ID, "config_sync"), ConfigSyncPayload::new);

    public ConfigSyncPayload(FriendlyByteBuf buf) {
        this(buf.readMap(FriendlyByteBuf::readUtf, FriendlyByteBuf::readUtf));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeMap(values, FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeUtf);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
