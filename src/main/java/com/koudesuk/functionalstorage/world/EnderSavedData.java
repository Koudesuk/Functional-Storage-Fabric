package com.koudesuk.functionalstorage.world;

import com.mojang.serialization.Codec;
import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.inventory.EnderInventoryHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;

public class EnderSavedData extends SavedData {

    public static final String NAME = "FunctionalStorageEnder";
    private HashMap<String, EnderInventoryHandler> itemHandlers = new HashMap<>();
    private Level level;

    public EnderSavedData(Level level) {
        this.level = level;
    }

    public EnderSavedData() {
        this(null);
    }

    public static EnderSavedData getInstance(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            EnderSavedData data = serverLevel.getDataStorage().computeIfAbsent(type(serverLevel));
            data.level = serverLevel;
            return data;
        }
        return new EnderSavedData(level);
    }

    private static SavedDataType<EnderSavedData> type(ServerLevel level) {
        net.minecraft.core.HolderLookup.Provider registries = level.registryAccess();
        Codec<EnderSavedData> codec = CompoundTag.CODEC.xmap(
                tag -> load(tag, registries, level),
                data -> data.save(new CompoundTag(), registries));
        return new SavedDataType<>(
                Identifier.fromNamespaceAndPath(FunctionalStorage.MOD_ID, "functional_storage_ender"),
                () -> new EnderSavedData(level),
                codec,
                DataFixTypes.SAVED_DATA_COMMAND_STORAGE);
    }

    public static EnderSavedData load(CompoundTag compoundTag, net.minecraft.core.HolderLookup.Provider registries,
            Level level) {
        EnderSavedData manager = new EnderSavedData(level);
        manager.itemHandlers = new HashMap<>();
        if (compoundTag.contains("Ender")) {
            CompoundTag backpacks = compoundTag.getCompoundOrEmpty("Ender");
            for (String s : backpacks.keySet()) {
                EnderInventoryHandler hander = new EnderInventoryHandler(s, manager);
                hander.deserializeNBT(backpacks.getCompoundOrEmpty(s), registries);
                manager.itemHandlers.put(s, hander);
            }
        }
        return manager;
    }

    public CompoundTag save(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag nbt = new CompoundTag();
        itemHandlers.forEach((s, iItemHandler) -> nbt.put(s, iItemHandler.serializeNBT(registries)));
        tag.put("Ender", nbt);
        return tag;
    }

    public EnderInventoryHandler getFrequency(String string) {
        return itemHandlers.computeIfAbsent(string, s -> new EnderInventoryHandler(s, this));
    }
}
