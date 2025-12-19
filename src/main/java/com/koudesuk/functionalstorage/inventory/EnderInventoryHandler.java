package com.koudesuk.functionalstorage.inventory;

import com.koudesuk.functionalstorage.util.DrawerType;
import com.koudesuk.functionalstorage.world.EnderSavedData;
import net.minecraft.nbt.CompoundTag;

public class EnderInventoryHandler extends BigInventoryHandler {

    public static String NBT_LOCKED = "Locked";
    public static String NBT_VOID = "Void";

    private final EnderSavedData manager;
    private String frequency;
    private boolean locked;
    private boolean voidItems;

    public EnderInventoryHandler(String frequency, EnderSavedData manager) {
        super(DrawerType.X_1);
        this.manager = manager;
        this.frequency = frequency;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag compoundTag = super.serializeNBT();
        compoundTag.putBoolean(NBT_LOCKED, this.locked);
        compoundTag.putBoolean(NBT_VOID, this.voidItems);
        return compoundTag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);
        this.locked = nbt.getBoolean(NBT_LOCKED);
        this.voidItems = nbt.getBoolean(NBT_VOID);
    }

    @Override
    public void onChange() {
        if (manager != null)
            manager.setDirty();
    }

    @Override
    public int getMultiplier() {
        return 4; // Ender Drawers have 4x base capacity
    }

    @Override
    public boolean isVoid() {
        return voidItems;
    }

    @Override
    public boolean hasDowngrade() {
        return false;
    }

    @Override
    public boolean isLocked() {
        return locked;
    }

    @Override
    public boolean isCreative() {
        return false;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
        onChange();
    }

    public void setVoidItems(boolean voidItems) {
        this.voidItems = voidItems;
        onChange();
    }

    public String getFrequency() {
        return frequency;
    }
}
