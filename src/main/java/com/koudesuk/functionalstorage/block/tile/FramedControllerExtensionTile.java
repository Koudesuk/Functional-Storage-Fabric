package com.koudesuk.functionalstorage.block.tile;

import com.koudesuk.functionalstorage.client.model.FramedDrawerModelData;
import com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Tile entity for the Framed Controller Access Point.
 * Extends ControllerExtensionTile and adds support for custom textures via
 * FramedDrawerModelData.
 */
@SuppressWarnings("deprecation")
public class FramedControllerExtensionTile extends ControllerExtensionTile implements RenderAttachmentBlockEntity {

    private FramedDrawerModelData framedDrawerModelData;

    public FramedControllerExtensionTile(BlockPos pos, BlockState state) {
        super(FunctionalStorageBlockEntities.FRAMED_CONTROLLER_EXTENSION, pos, state);
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
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }
}
