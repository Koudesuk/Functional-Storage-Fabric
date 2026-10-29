package com.koudesuk.functionalstorage;

import com.koudesuk.functionalstorage.data.FunctionalStorageBlockTagProvider;
import com.koudesuk.functionalstorage.data.FunctionalStorageFluidTagProvider;
import com.koudesuk.functionalstorage.data.FunctionalStorageItemTagProvider;
import com.koudesuk.functionalstorage.data.FunctionalStorageModelProvider;
import com.koudesuk.functionalstorage.data.FunctionalStorageRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.data.DataProvider;

public class FunctionalStorageFabricDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider((DataProvider.Factory<FunctionalStorageModelProvider>) FunctionalStorageModelProvider::new);
        pack.addProvider(FunctionalStorageBlockTagProvider::new);
        pack.addProvider(FunctionalStorageItemTagProvider::new);
        pack.addProvider(FunctionalStorageFluidTagProvider::new);
        pack.addProvider(FunctionalStorageRecipeProvider::new);
    }
}
