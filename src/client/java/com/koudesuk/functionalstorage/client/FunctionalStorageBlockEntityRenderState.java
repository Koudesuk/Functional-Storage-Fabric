package com.koudesuk.functionalstorage.client;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;

public class FunctionalStorageBlockEntityRenderState<T extends BlockEntity> extends BlockEntityRenderState {
    public T blockEntity;
    public float partialTicks;
}
