package com.koudesuk.functionalstorage.mixin;

import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * The pattern of a shaped recipe, which CopyComponentsRecipe passes on like the original (NeoForge makes
 * the field public there).
 */
@Mixin(ShapedRecipe.class)
public interface ShapedRecipeAccessor {

    @Accessor("pattern")
    ShapedRecipePattern functionalstorage$getPattern();
}
