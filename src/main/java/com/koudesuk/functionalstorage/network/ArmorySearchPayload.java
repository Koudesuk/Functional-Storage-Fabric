package com.koudesuk.functionalstorage.network;

import com.koudesuk.functionalstorage.FunctionalStorage;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * Client to server: the search text and scroll row of the open armory cabinet screen, so both sides show the
 * same slots. 1.20.1's container button packet only carries a byte, too little for either.
 */
public record ArmorySearchPayload(int containerId, String query, int scrollRow) implements FabricPacket {
    public static final int MAX_QUERY_LENGTH = 50;
    public static final PacketType<ArmorySearchPayload> TYPE = PacketType.create(
            new ResourceLocation(FunctionalStorage.MOD_ID, "armory_search"), ArmorySearchPayload::new);

    public ArmorySearchPayload(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readUtf(MAX_QUERY_LENGTH), buf.readVarInt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        buf.writeUtf(query, MAX_QUERY_LENGTH);
        buf.writeVarInt(scrollRow);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
