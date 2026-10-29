package com.koudesuk.functionalstorage.block.tile;

import com.koudesuk.functionalstorage.client.model.FramedDrawerModelData;
import com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities;
import com.koudesuk.functionalstorage.util.DrawerType;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;

public class FramedDrawerTile extends DrawerTile {

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

    public boolean isEverythingEmpty() {
        // A broken drawer keeps its priority, like the original
        if (getPriority() != 0)
            return false;
        for (StorageView<ItemVariant> view : getStorage()) {
            if (!view.isResourceBlank() && view.getAmount() > 0) {
                return false;
            }
        }
        return true;
    }
}
