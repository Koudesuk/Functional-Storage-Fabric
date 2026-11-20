package com.koudesuk.functionalstorage.registry;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.inventory.ArmoryCabinetMenu;
import com.koudesuk.functionalstorage.inventory.DrawerMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;

public class FunctionalStorageMenus {

    public static MenuType<DrawerMenu> DRAWER;
    public static MenuType<ArmoryCabinetMenu> ARMORY_CABINET;

    public static void register() {
        DRAWER = Registry.register(BuiltInRegistries.MENU, new ResourceLocation(FunctionalStorage.MOD_ID, "drawer"),
                new net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType<>(DrawerMenu::new));
        // The original's armory cabinet screen: the client gets the contents with the opening data
        ARMORY_CABINET = Registry.register(BuiltInRegistries.MENU,
                new ResourceLocation(FunctionalStorage.MOD_ID, "armory_cabinet"),
                new net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType<>(ArmoryCabinetMenu::new));
    }
}
