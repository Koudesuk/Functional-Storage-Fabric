package com.koudesuk.functionalstorage.item;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

/**
 * Tooltip image of a drawer item that carries a broken drawer's contents. The client turns it into
 * icons (DrawerClientTooltipComponent), since reading the stored items needs the level's registries.
 */
public record DrawerContentsTooltip(ItemStack drawer) implements TooltipComponent {
}
