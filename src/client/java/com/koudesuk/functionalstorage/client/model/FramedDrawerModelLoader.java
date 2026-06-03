package com.koudesuk.functionalstorage.client.model;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;

public class FramedDrawerModelLoader implements ModelLoadingPlugin {
    public void initialize(Context pluginContext) {
        onInitializeModelLoader(pluginContext);
    }

    public void onInitializeModelLoader(Context pluginContext) {
    }
}
