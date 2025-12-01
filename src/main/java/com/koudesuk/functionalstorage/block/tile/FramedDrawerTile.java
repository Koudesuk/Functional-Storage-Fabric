package com.koudesuk.functionalstorage.block.tile;

import com.koudesuk.functionalstorage.client.model.FramedDrawerModelData;
import com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities;
import com.koudesuk.functionalstorage.util.DrawerType;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;

public class FramedDrawerTile extends DrawerTile implements RenderAttachmentBlockEntity {

    private FramedDrawerModelData framedDrawerModelData;

    public FramedDrawerTile(BlockPos pos, BlockState state, DrawerType type) {
        super(FunctionalStorageBlockEntities.FRAMED_DRAWER, pos, state);
        this.framedDrawerModelData = new FramedDrawerModelData(new HashMap<>());
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
        if (tag.contains("Style")) {
            this.framedDrawerModelData.deserializeNBT(tag.getCompound("Style"));
        }
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

    public boolean isEverythingEmpty() {
        for (StorageView<ItemVariant> view : getStorage()) {
            if (!view.isResourceBlank() && view.getAmount() > 0) {
                return false;
            }
        }
        return true;
    }
}
