package com.koudesuk.functionalstorage.block.tile;

import com.koudesuk.functionalstorage.client.model.FramedDrawerModelData;
import com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Tile entity for the Framed Controller Access Point.
 * Extends ControllerExtensionTile and adds support for custom textures via
 * FramedDrawerModelData.
 */
@SuppressWarnings("deprecation")
public class FramedControllerExtensionTile extends ControllerExtensionTile {

    private FramedDrawerModelData framedDrawerModelData;

    public FramedControllerExtensionTile(BlockPos pos, BlockState state) {
        super(FunctionalStorageBlockEntities.FRAMED_CONTROLLER_EXTENSION, pos, state);
        this.framedDrawerModelData = new FramedDrawerModelData(null);
    }

    // Read by the framed block model through FabricBlockGetter.getBlockEntityRenderData
    @Override
    public @Nullable Object getRenderData() {
        return framedDrawerModelData;
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
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
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }
}
