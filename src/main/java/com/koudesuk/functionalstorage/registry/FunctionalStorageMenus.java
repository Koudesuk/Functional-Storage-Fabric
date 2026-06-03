package com.koudesuk.functionalstorage.registry;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.inventory.DrawerMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;

public class FunctionalStorageMenus {

        public static MenuType<DrawerMenu> DRAWER;

        public static void register() {
                DRAWER = Registry.register(BuiltInRegistries.MENU,
                                Identifier.fromNamespaceAndPath(FunctionalStorage.MOD_ID, "drawer"),
                                new net.fabricmc.fabric.api.menu.v1.ExtendedMenuType<>(
                                                (syncId, inventory, payload) -> new DrawerMenu(syncId, inventory,
                                                                payload.pos()),
                                                com.koudesuk.functionalstorage.network.BlockPosPayload.STREAM_CODEC));
        }
}
