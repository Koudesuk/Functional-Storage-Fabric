package com.koudesuk.functionalstorage.network;

import com.koudesuk.functionalstorage.FunctionalStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client to server: the priority typed into a drawer's screen, like the original's DrawerPriorityMessage.
 */
public record DrawerPriorityPayload(BlockPos pos, int priority) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DrawerPriorityPayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(FunctionalStorage.MOD_ID, "drawer_priority"));

    public static final StreamCodec<FriendlyByteBuf, DrawerPriorityPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, DrawerPriorityPayload::pos,
            ByteBufCodecs.VAR_INT, DrawerPriorityPayload::priority,
            DrawerPriorityPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
