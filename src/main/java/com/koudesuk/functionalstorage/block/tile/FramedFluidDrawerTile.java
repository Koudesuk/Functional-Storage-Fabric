package com.koudesuk.functionalstorage.block.tile;

import com.koudesuk.functionalstorage.client.model.FramedDrawerModelData;
import com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities;
import com.koudesuk.functionalstorage.util.DrawerType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;

/**
 * A fluid drawer that takes the look of other blocks, like the original's FramedFluidDrawerTile.
 */
public class FramedFluidDrawerTile extends FluidDrawerTile {

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

    // Read by the framed block model through FabricBlockGetter.getBlockEntityRenderData
    @Override
    public Object getRenderData() {
        return framedDrawerModelData;
    }
}
