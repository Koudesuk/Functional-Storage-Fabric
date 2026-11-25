package com.koudesuk.functionalstorage.block.tile;

import com.koudesuk.functionalstorage.client.model.FramedDrawerModelData;
import com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities;
import com.koudesuk.functionalstorage.util.DrawerType;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;

/**
 * A fluid drawer that takes the look of other blocks, like the original's FramedFluidDrawerTile.
 */
public class FramedFluidDrawerTile extends FluidDrawerTile implements RenderAttachmentBlockEntity {

    private FramedDrawerModelData framedDrawerModelData;

    public FramedFluidDrawerTile(BlockPos pos, BlockState state, DrawerType type) {
        super(FunctionalStorageBlockEntities.FRAMED_FLUID_DRAWER, pos, state, type);
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
    public void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Style")) {
            this.framedDrawerModelData = FramedDrawerModelData.fromNBT(tag.getCompound("Style"));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.framedDrawerModelData != null) {
            tag.put("Style", this.framedDrawerModelData.serializeNBT(registries));
        }
    }

    // Read by the framed block model, like the other framed drawers
    @Override
    public Object getRenderAttachmentData() {
        return framedDrawerModelData;
    }
}
