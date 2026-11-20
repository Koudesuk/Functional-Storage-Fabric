package com.koudesuk.functionalstorage.data;

import com.koudesuk.functionalstorage.util.StorageTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

/**
 * Same as the original's FunctionalStorageFluidTagsProvider.
 */
public class FunctionalStorageFluidTagProvider extends FabricTagProvider.FluidTagProvider {

    public FunctionalStorageFluidTagProvider(FabricDataOutput output,
            CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider arg) {
        // Empty on purpose, for data packs to fill
        getOrCreateTagBuilder(StorageTags.FLUID_DRAWER_STORAGE_DENYLIST);
    }
}
