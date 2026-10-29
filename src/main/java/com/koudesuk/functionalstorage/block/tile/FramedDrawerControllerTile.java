package com.koudesuk.functionalstorage.block.tile;

import com.koudesuk.functionalstorage.client.model.FramedDrawerModelData;
import com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class FramedDrawerControllerTile extends StorageControllerTile {

    private FramedDrawerModelData framedDrawerModelData;

    public FramedDrawerControllerTile(BlockPos pos, BlockState blockState) {
        super(FunctionalStorageBlockEntities.FRAMED_DRAWER_CONTROLLER, pos, blockState);
        this.framedDrawerModelData = new FramedDrawerModelData(null);
    }

    // Read by the framed block model through FabricBlockGetter.getBlockEntityRenderData
    @Override
    public @Nullable Object getRenderData() {
        return framedDrawerModelData;
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

    public FramedDrawerModelData getFramedDrawerModelData() {
        return framedDrawerModelData;
    }

    public void setFramedDrawerModelData(FramedDrawerModelData framedDrawerModelData) {
        this.framedDrawerModelData = framedDrawerModelData;
        this.setChanged();
    }
}
