package com.koudesuk.functionalstorage.client;

import com.koudesuk.functionalstorage.client.gui.FunctionalStorageConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * "modmenu" entrypoint. Only Mod Menu loads this class, so Mod Menu stays optional.
 */
public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return FunctionalStorageConfigScreen::new;
    }
}
