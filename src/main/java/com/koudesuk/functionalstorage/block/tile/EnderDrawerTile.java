package com.koudesuk.functionalstorage.block.tile;

import com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class EnderDrawerTile extends ControllableDrawerTile<EnderDrawerTile> {

    private String frequency;

    public EnderDrawerTile(BlockPos pos, BlockState state) {
        super(FunctionalStorageBlockEntities.ENDER_DRAWER, pos, state);
    }

    @Override
    public int getStorageSlotAmount() {
        return 0;
    }

    public void setFrequency(String frequency) {
        if (frequency == null)
            return;
        this.frequency = frequency;
        this.setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public String getFrequency() {
        return frequency;
    }

    @Override
    public void load(net.minecraft.nbt.CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Frequency")) {
            this.frequency = tag.getString("Frequency");
        }
    }

    @Override
    protected void saveAdditional(net.minecraft.nbt.CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.frequency != null) {
            tag.putString("Frequency", this.frequency);
        }
    }
}
