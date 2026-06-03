package com.koudesuk.functionalstorage.data;

import java.util.concurrent.CompletableFuture;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

public class FunctionalStorageModelProvider implements DataProvider {
    public FunctionalStorageModelProvider(PackOutput output) {
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
        return "Functional Storage Models";
    }
}
