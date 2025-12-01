package com.koudesuk.functionalstorage.block.tile;

import com.koudesuk.functionalstorage.client.model.FramedDrawerModelData;
import com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class FramedDrawerControllerTile extends StorageControllerTile implements RenderAttachmentBlockEntity {

    private FramedDrawerModelData framedDrawerModelData;

    public FramedDrawerControllerTile(BlockPos pos, BlockState blockState) {
        super(FunctionalStorageBlockEntities.FRAMED_DRAWER_CONTROLLER, pos, blockState);
        this.framedDrawerModelData = new FramedDrawerModelData(null);
    }

    @Override
    public @Nullable Object getRenderAttachmentData() {
        return framedDrawerModelData;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Style")) {
            this.framedDrawerModelData = FramedDrawerModelData.fromNBT(tag.getCompound("Style"));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.framedDrawerModelData != null) {
            tag.put("Style", this.framedDrawerModelData.serializeNBT());
        }
    }

    public FramedDrawerModelData getFramedDrawerModelData() {
        return framedDrawerModelData;
    }

    public void setFramedDrawerModelData(FramedDrawerModelData framedDrawerModelData) {
        this.framedDrawerModelData = framedDrawerModelData;
        this.setChanged();
    }
}
