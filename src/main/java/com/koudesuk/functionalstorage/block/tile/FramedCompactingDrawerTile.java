package com.koudesuk.functionalstorage.block.tile;

import com.koudesuk.functionalstorage.client.model.FramedDrawerModelData;
import com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public class FramedCompactingDrawerTile extends CompactingDrawerTile implements RenderAttachmentBlockEntity {

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
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.framedDrawerModelData = FramedDrawerModelData.fromNBT(tag.getCompound("Style"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.framedDrawerModelData != null) {
            tag.put("Style", this.framedDrawerModelData.serializeNBT());
        }
    }

    @Override
    public Object getRenderAttachmentData() {
        return framedDrawerModelData;
    }
}
