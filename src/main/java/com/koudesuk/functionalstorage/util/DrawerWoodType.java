package com.koudesuk.functionalstorage.util;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public enum DrawerWoodType implements IWoodType {
    OAK(Blocks.OAK_PLANKS, "oak"),
    SPRUCE(Blocks.SPRUCE_PLANKS, "spruce"),
    BIRCH(Blocks.BIRCH_PLANKS, "birch"),
    JUNGLE(Blocks.JUNGLE_PLANKS, "jungle"),
    ACACIA(Blocks.ACACIA_PLANKS, "acacia"),
    DARK_OAK(Blocks.DARK_OAK_PLANKS, "dark_oak"),
    CRIMSON(Blocks.CRIMSON_PLANKS, "crimson"),
    WARPED(Blocks.WARPED_PLANKS, "warped"),
    MANGROVE(Blocks.MANGROVE_PLANKS, "mangrove"),
    CHERRY(Blocks.CHERRY_PLANKS, "cherry"),
    FRAMED(Blocks.AIR, "framed");

    private final Block planks;
    private final String name;

    DrawerWoodType(Block planks, String name) {
        this.planks = planks;
        this.name = name;
    }

    @Override
    public Block getPlanks() {
        return planks;
    }

    @Override
    public String getName() {
        return name;
    }
}
