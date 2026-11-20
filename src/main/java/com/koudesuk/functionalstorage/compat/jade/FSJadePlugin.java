package com.koudesuk.functionalstorage.compat.jade;

import com.koudesuk.functionalstorage.block.ArmoryCabinetBlock;
import com.koudesuk.functionalstorage.block.CompactingDrawerBlock;
import com.koudesuk.functionalstorage.block.DrawerBlock;
import com.koudesuk.functionalstorage.block.EnderDrawerBlock;
import com.koudesuk.functionalstorage.block.SimpleCompactingDrawerBlock;
import com.koudesuk.functionalstorage.block.tile.ArmoryCabinetTile;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Jade support, a port of the original's FSJadePlugin, loaded through the "jade" entrypoint. The original's
 * Drawer base class is four block classes here (fluid and framed drawers extend DrawerBlock).
 */
@WailaPlugin
public class FSJadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(DrawerComponentProvider.INSTANCE, DrawerBlock.class);
        registration.registerBlockComponent(DrawerComponentProvider.INSTANCE, CompactingDrawerBlock.class);
        registration.registerBlockComponent(DrawerComponentProvider.INSTANCE, SimpleCompactingDrawerBlock.class);
        registration.registerBlockComponent(DrawerComponentProvider.INSTANCE, EnderDrawerBlock.class);
        registration.registerBlockComponent(ArmoryComponentProvider.INSTANCE, ArmoryCabinetBlock.class);
    }

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(ArmoryComponentProvider.INSTANCE, ArmoryCabinetTile.class);
    }
}
