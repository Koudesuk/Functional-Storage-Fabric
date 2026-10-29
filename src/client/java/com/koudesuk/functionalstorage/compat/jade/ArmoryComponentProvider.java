package com.koudesuk.functionalstorage.compat.jade;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.block.config.FunctionalStorageConfig;
import com.koudesuk.functionalstorage.block.tile.ArmoryCabinetTile;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.JadeUI;

/**
 * Shows how full the armory cabinet is, like the original's ArmoryComponentProvider.
 */
public enum ArmoryComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    public static final Identifier ID = Identifier.fromNamespaceAndPath(FunctionalStorage.MOD_ID, "armory");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        String key = getUid().toLanguageKey();
        if (accessor.getBlockEntity() instanceof ArmoryCabinetTile && accessor.getServerData().contains(key)) {
            int count = accessor.getServerData().getIntOr(key, 0);
            tooltip.add(JadeUI.text(Component.literal(count + " / " + FunctionalStorageConfig.ARMORY_CABINET_SIZE.get()))
                    .offset(4, (int) ((.86f * 18 - 10) / 2)));
        }
    }

    @Override
    public int getDefaultPriority() {
        return 999;
    }

    @Override
    public Identifier getUid() {
        return ID;
    }
}
