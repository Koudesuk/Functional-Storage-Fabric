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
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("handler")) {
            this.handler.deserializeNBT(tag.getCompound("handler"), registries);
        }
    }

    /**
     * Public method for loading tile data from an ItemStack's component data.
     * This is used in Block.setPlacedBy() to restore tile state.
     */
    public void loadFromTag(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        this.loadAdditional(tag, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("handler", this.handler.serializeNBT(registries));
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    public boolean isEverythingEmpty() {
        return handler.isEmpty();
    }
}
