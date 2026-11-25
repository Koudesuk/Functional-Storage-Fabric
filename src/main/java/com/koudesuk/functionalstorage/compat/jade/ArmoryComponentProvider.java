package com.koudesuk.functionalstorage.compat.jade;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.block.tile.ArmoryCabinetTile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;

/**
 * How full the armory cabinet is, the original's ArmoryComponentProvider. The count comes from the server,
 * since the client's copy of the cabinet is not kept up to date.
 */
public enum ArmoryComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FunctionalStorage.MOD_ID, "armory");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        String key = getUid().toLanguageKey();
        if (accessor.getBlockEntity() instanceof ArmoryCabinetTile armory && accessor.getServerData().contains(key)) {
            int count = accessor.getServerData().getInt(key);
            tooltip.add(IElementHelper.get()
                    .text(Component.literal(count + " / " + armory.handler.getContainerSize()))
                    .translate(new Vec2(4, (.86f * 18 - 10) / 2)));
        }
    }

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
    public int getDefaultPriority() {
        return 999;
    }

    @Override
    public ResourceLocation getUid() {
        return ID;
    }
}
