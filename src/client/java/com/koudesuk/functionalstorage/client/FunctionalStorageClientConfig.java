package com.koudesuk.functionalstorage.client;

import com.koudesuk.functionalstorage.block.config.ConfigFile;
import com.koudesuk.functionalstorage.block.config.ConfigFile.DoubleValue;
import com.koudesuk.functionalstorage.block.config.ConfigFile.IntValue;

/**
 * Rendering settings, stored in config/functionalstorage-client.properties. Never synced.
 */
public final class FunctionalStorageClientConfig {

    public static final ConfigFile FILE = new ConfigFile("functionalstorage-client", """
            Functional Storage - client settings
            Restart the game after editing this file.""");

    public static final IntValue DRAWER_RENDER_RANGE = FILE.defineInt("render.drawer_render_range",
            "Distance, in blocks, within which drawers render their contents. Block entities stop rendering past 64.",
            16, 1, 64);

    public static final DoubleValue DRAWER_RENDER_THICKNESS = FILE.defineDouble("render.drawer_render_thickness",
            "Thickness of the 3D item and block displays on drawer fronts.",
            0.125, 0.05, 0.75);

    private FunctionalStorageClientConfig() {
    }
}
