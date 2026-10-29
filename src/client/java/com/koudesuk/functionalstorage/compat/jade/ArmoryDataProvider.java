package com.koudesuk.functionalstorage.compat.jade;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.block.tile.ArmoryCabinetTile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

/**
 * Server side: how many items the armory cabinet holds, since its client copy is not kept up to date.
 */
public enum ArmoryDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final Identifier ID = Identifier.fromNamespaceAndPath(FunctionalStorage.MOD_ID, "armory");

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof ArmoryCabinetTile armory) {
            int count = 0;
            for (int slot = 0; slot < armory.handler.getContainerSize(); slot++) {
                if (!armory.handler.getItem(slot).isEmpty())
                    count++;
            }
            data.putInt(getUid().toLanguageKey(), count);
        }
    }

    @Override
    public Identifier getUid() {
        return ID;
    }
}
