package com.koudesuk.functionalstorage;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import com.koudesuk.functionalstorage.data.FunctionalStorageModelProvider;
import com.koudesuk.functionalstorage.data.FunctionalStorageBlockTagProvider;
import com.koudesuk.functionalstorage.data.FunctionalStorageItemTagProvider;
import com.koudesuk.functionalstorage.data.FunctionalStorageRecipeProvider;

public class FunctionalStorageFabricDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(FunctionalStorageModelProvider::new);
        pack.addProvider(FunctionalStorageBlockTagProvider::new);
        pack.addProvider(FunctionalStorageItemTagProvider::new);
        pack.addProvider(FunctionalStorageRecipeProvider::new);
    }
}
