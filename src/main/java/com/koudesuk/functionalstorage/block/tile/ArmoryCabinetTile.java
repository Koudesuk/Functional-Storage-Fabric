package com.koudesuk.functionalstorage.block.tile;

import com.koudesuk.functionalstorage.inventory.ArmoryCabinetInventoryHandler;
import com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ArmoryCabinetTile extends BlockEntity {

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
}
