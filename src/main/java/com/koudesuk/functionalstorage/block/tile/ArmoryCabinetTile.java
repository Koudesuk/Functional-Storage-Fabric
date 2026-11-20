package com.koudesuk.functionalstorage.block.tile;

import com.koudesuk.functionalstorage.inventory.ArmoryCabinetInventoryHandler;
import com.koudesuk.functionalstorage.inventory.ArmoryCabinetMenu;
import com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ArmoryCabinetTile extends BlockEntity implements ExtendedScreenHandlerFactory {

    public ArmoryCabinetInventoryHandler handler;

    public ArmoryCabinetTile(BlockPos pos, BlockState state) {
        super(FunctionalStorageBlockEntities.ARMORY_CABINET, pos, state);
        this.handler = new ArmoryCabinetInventoryHandler() {
            @Override
            public void onChange() {
                ArmoryCabinetTile.this.setChanged();
            }
        };
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("handler")) {
            this.handler.deserializeNBT(tag.getCompound("handler"));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("handler", this.handler.serializeNBT());
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public boolean isEverythingEmpty() {
        return handler.isEmpty();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.functionalstorage.armory_cabinet");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ArmoryCabinetMenu(id, inventory, handler, getBlockPos());
    }

    // The client gets the contents when the screen opens, like the original
    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
        ArmoryCabinetMenu.writeOpenData(buf, getBlockPos(), handler);
    }
}
