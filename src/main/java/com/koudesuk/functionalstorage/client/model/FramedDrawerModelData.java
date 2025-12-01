package com.koudesuk.functionalstorage.client.model;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.HashMap;
import java.util.Map;

public class FramedDrawerModelData {

    private Map<String, Item> design;
    private String code = "";

    public FramedDrawerModelData(Map<String, Item> design) {
        this.design = design;
        if (this.design == null)
            this.design = new HashMap<>();
        this.generateCode();
    }

    public Map<String, Item> getDesign() {
        return design;
    }

    public CompoundTag serializeNBT() {
        CompoundTag compoundTag = new CompoundTag();
        design.forEach((s, item) -> compoundTag.putString(s, BuiltInRegistries.ITEM.getKey(item).toString()));
        return compoundTag;
    }

    public void deserializeNBT(CompoundTag nbt) {
        design = new HashMap<>();
        for (String allKey : nbt.getAllKeys()) {
            design.put(allKey, BuiltInRegistries.ITEM.get(new ResourceLocation(nbt.getString(allKey))));
        }
        this.generateCode();
    }

    public static FramedDrawerModelData fromNBT(CompoundTag nbt) {
        HashMap<String, Item> design = new HashMap<>();
        for (String allKey : nbt.getAllKeys()) {
            design.put(allKey, BuiltInRegistries.ITEM.get(new ResourceLocation(nbt.getString(allKey))));
        }
        return new FramedDrawerModelData(design);
    }

    private void generateCode() {
        this.code = "";
        if (this.design != null) {
            this.design.forEach((s, item) -> {
                this.code += (s + BuiltInRegistries.ITEM.getKey(item).toString());
            });
        }
    }

    public String getCode() {
        return code;
    }
}
