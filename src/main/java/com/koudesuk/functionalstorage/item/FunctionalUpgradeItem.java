package com.koudesuk.functionalstorage.item;

import com.koudesuk.functionalstorage.item.component.DelegateToItemBehavior;
import com.koudesuk.functionalstorage.registry.FSAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * The original's addon base class: a utility upgrade whose {@link #work} runs every tick in a drawer.
 *
 * @deprecated Like the original, use an item with the {@link FSAttachments#FUNCTIONAL_BEHAVIOR} component.
 */
@Deprecated(forRemoval = true)
public class FunctionalUpgradeItem extends Item {

    public FunctionalUpgradeItem(Properties properties) {
        super(properties.component(FSAttachments.FUNCTIONAL_BEHAVIOR, DelegateToItemBehavior.INSTANCE));
    }

    public void work(Level level, BlockPos pos, ItemStack upgradeStack) {
    }
}
