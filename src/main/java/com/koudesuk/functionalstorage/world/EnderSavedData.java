package com.koudesuk.functionalstorage.world;

import com.koudesuk.functionalstorage.inventory.EnderInventoryHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

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

    public static EnderSavedData getInstance(Level level){
        if (level instanceof ServerLevel serverLevel){
            return serverLevel.getDataStorage().computeIfAbsent(
                tag -> load(tag, serverLevel),
                () -> new EnderSavedData(serverLevel),
                NAME
            );
        }
        return new EnderSavedData(level);
    }

    public static EnderSavedData load(CompoundTag compoundTag, Level level) {
        EnderSavedData manager = new EnderSavedData(level);
        manager.itemHandlers = new HashMap<>();
        if (compoundTag.contains("Ender")) {
            CompoundTag backpacks = compoundTag.getCompound("Ender");
            for (String s : backpacks.getAllKeys()) {
                EnderInventoryHandler hander = new EnderInventoryHandler(s, manager);
                hander.deserializeNBT(backpacks.getCompound(s));
                manager.itemHandlers.put(s, hander);
            }
        }
        return manager;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag nbt = new CompoundTag();
        itemHandlers.forEach((s, iItemHandler) -> nbt.put(s, iItemHandler.serializeNBT()));
        tag.put("Ender", nbt);
        return tag;
    }

    public EnderInventoryHandler getFrequency(String string){
        return itemHandlers.computeIfAbsent(string, s -> new EnderInventoryHandler(s, this));
    }
}
