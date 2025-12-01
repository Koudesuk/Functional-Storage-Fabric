package com.koudesuk.functionalstorage.registry;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.inventory.DrawerMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;

public class FunctionalStorageMenus {

    public static MenuType<DrawerMenu> DRAWER;

    public static void register() {
        DRAWER = Registry.register(BuiltInRegistries.MENU, new ResourceLocation(FunctionalStorage.MOD_ID, "drawer"),
                new net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType<>(DrawerMenu::new));
    }
}
