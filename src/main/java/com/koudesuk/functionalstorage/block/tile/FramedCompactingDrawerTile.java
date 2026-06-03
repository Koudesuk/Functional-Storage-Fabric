package com.koudesuk.functionalstorage.block.tile;

import com.koudesuk.functionalstorage.client.model.FramedDrawerModelData;
import com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public class FramedCompactingDrawerTile extends CompactingDrawerTile {

    private FramedDrawerModelData framedDrawerModelData;

    public FramedCompactingDrawerTile(BlockPos pos, BlockState state) {
        super(FunctionalStorageBlockEntities.FRAMED_COMPACTING_DRAWER, pos, state);
    }

    public FramedDrawerModelData getFramedDrawerModelData() {
        return framedDrawerModelData;
    }

    public void setFramedDrawerModelData(FramedDrawerModelData framedDrawerModelData) {
        this.framedDrawerModelData = framedDrawerModelData;
        this.setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        input.read("Style", CompoundTag.CODEC)
                .ifPresent(tag -> this.framedDrawerModelData = FramedDrawerModelData.fromNBT(tag));
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);
        if (this.framedDrawerModelData != null) {
            output.store("Style", CompoundTag.CODEC, this.framedDrawerModelData.serializeNBT());
        }
    }

    public Object getRenderAttachmentData() {
        return framedDrawerModelData;
    }
}
