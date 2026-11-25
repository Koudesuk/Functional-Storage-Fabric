package com.koudesuk.functionalstorage.registry;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.inventory.DrawerMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;

public class FunctionalStorageMenus {

        public static MenuType<DrawerMenu> DRAWER;
        public static MenuType<com.koudesuk.functionalstorage.inventory.ArmoryCabinetMenu> ARMORY_CABINET;

        public static void register() {
                DRAWER = Registry.register(BuiltInRegistries.MENU,
                                ResourceLocation.fromNamespaceAndPath(FunctionalStorage.MOD_ID, "drawer"),
                                new net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType<>(
                                                (syncId, inventory, payload) -> new DrawerMenu(syncId, inventory,
                                                                payload.pos()),
                                                com.koudesuk.functionalstorage.network.BlockPosPayload.STREAM_CODEC));
                ARMORY_CABINET = Registry.register(BuiltInRegistries.MENU,
                                ResourceLocation.fromNamespaceAndPath(FunctionalStorage.MOD_ID, "armory_cabinet"),
                                new net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType<>(
                                                com.koudesuk.functionalstorage.inventory.ArmoryCabinetMenu::new,
                                                com.koudesuk.functionalstorage.inventory.ArmoryCabinetMenu.OpenData.STREAM_CODEC));
        }
}
