package com.koudesuk.functionalstorage.compat.jade;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.block.EnderDrawerBlock;
import com.koudesuk.functionalstorage.block.tile.ControllableDrawerTile;
import com.koudesuk.functionalstorage.block.tile.EnderDrawerTile;
import com.koudesuk.functionalstorage.block.tile.FluidDrawerTile;
import com.koudesuk.functionalstorage.block.tile.ItemControllableDrawerTile;
import com.koudesuk.functionalstorage.inventory.BigInventoryHandler;
import com.koudesuk.functionalstorage.inventory.CompactingInventoryHandler;
import com.koudesuk.functionalstorage.inventory.FluidInventoryHandler;
import com.koudesuk.functionalstorage.item.UpgradeItem;
import com.koudesuk.functionalstorage.util.CompactingUtil;
import com.koudesuk.functionalstorage.util.NumberUtils;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IBoxStyle;
import snownee.jade.api.ui.IElementHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * A drawer's contents, Ender frequency and (with the details key) upgrades, a port of the original's
 * DrawerComponentProvider. Jade's own storage lines are replaced by these.
 */
public enum DrawerComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    public static final ResourceLocation ITEM_STORAGE = new ResourceLocation("item_storage");
    public static final ResourceLocation FLUID_STORAGE = new ResourceLocation("fluid_storage");
    public static final ResourceLocation ID = new ResourceLocation(FunctionalStorage.MOD_ID, "drawer");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        tooltip.remove(ITEM_STORAGE);
        tooltip.remove(FLUID_STORAGE);
        if (!(accessor.getBlockEntity() instanceof ControllableDrawerTile<?> controllable))
            return;
        IElementHelper helper = IElementHelper.get();
        if (controllable instanceof ItemControllableDrawerTile<?> tile) {
            appendItems(tooltip, helper, tile);
        } else if (controllable instanceof FluidDrawerTile tile) {
            appendFluids(tooltip, helper, tile.getHandler(), tile.getDrawerType().getSlots());
        }
        if (controllable instanceof EnderDrawerTile ender && ender.getFrequency() != null) {
            ITooltip frequencyBox = helper.tooltip();
            tooltip.add(helper.text(Component.translatable("linkingtool.ender.frequency")));
            for (ItemStack stack : EnderDrawerBlock.getFrequencyDisplay(ender.getFrequency())) {
                frequencyBox.append(helper.item(stack));
            }
            tooltip.add(helper.box(frequencyBox, BoxStyle.DEFAULT));
        }
        if (accessor.showDetails()) {
            for (int i = 0; i < controllable.getUtilityUpgrades().getContainerSize(); i++) {
                ItemStack stack = controllable.getUtilityUpgrades().getItem(i);
                if (stack.getItem() instanceof UpgradeItem upgrade) {
                    Component description = upgrade.getDescription(stack, controllable);
                    if (description != null)
                        tooltip.add(description);
                }
            }
            int multiplier = controllable.getStorageMultiplier();
            if (multiplier > 1) {
                tooltip.add(Component.translatable("drawer.block.multiplier",
                        Component.literal("x" + multiplier).withStyle(ChatFormatting.GOLD)));
            }
        }
    }

    private static void appendItems(ITooltip tooltip, IElementHelper helper, ItemControllableDrawerTile<?> tile) {
        List<ItemStack> stacks = new ArrayList<>();
        List<Integer> amounts = new ArrayList<>();
        List<Integer> limits = new ArrayList<>();
        // On the client an Ender Drawer's storage is its synced copy of the frequency
        var storage = tile.getStorage();
        if (storage instanceof BigInventoryHandler big) {
            for (int slot = 0; slot < big.getStoredStacks().size(); slot++) {
                BigInventoryHandler.BigStack stored = big.getStoredStacks().get(slot);
                if (!stored.getStack().isEmpty()) {
                    stacks.add(stored.getStack());
                    amounts.add(big.isCreative() ? Integer.MAX_VALUE : stored.getAmount());
                    limits.add(big.getSlotLimit(slot));
                }
            }
        } else if (storage instanceof CompactingInventoryHandler compacting) {
            List<CompactingUtil.Result> results = compacting.getResultList();
            for (int slot = 0; slot < results.size(); slot++) {
                ItemStack result = results.get(slot).getResult();
                if (!result.isEmpty()) {
                    stacks.add(result);
                    amounts.add(compacting.isCreative() ? Integer.MAX_VALUE : compacting.getStackInSlot(slot).getCount());
                    limits.add(compacting.getSlotLimit(slot));
                }
            }
        }
        if (stacks.isEmpty())
            return;
        ITooltip contents = helper.tooltip();
        for (int i = 0; i < stacks.size(); i++) {
            // An empty locked slot still shows its item
            contents.add(helper.item(stacks.get(i).copyWithCount(1), 0.75f, "")
                    .size(new Vec2(0.75f * 18, 0.75f * 18)).translate(new Vec2(0, -1)));
            contents.append(helper.text(Component.literal("x " + NumberUtils.getFormatedBigNumber(amounts.get(i))
                    + " / " + NumberUtils.getFormatedBigNumber(limits.get(i))))
                    .translate(new Vec2(4, 1 + (.75f * 18 - 10) / 2)));
        }
        tooltip.add(helper.box(contents, IBoxStyle.Empty.INSTANCE));
    }

    private static void appendFluids(ITooltip tooltip, IElementHelper helper, FluidInventoryHandler handler,
            int slots) {
        ITooltip contents = helper.tooltip();
        boolean any = false;
        for (int slot = 0; slot < slots; slot++) {
            FluidVariant fluid = handler.getResource(slot);
            long amount = handler.getAmount(slot);
            if (fluid.isBlank() || amount <= 0)
                continue;
            any = true;
            contents.add(helper.fluid(JadeFluidObject.of(fluid.getFluid(), amount, fluid.copyNbt()))
                    .size(new Vec2(14, 14)));
            contents.append(helper.text(Component.empty().append(FluidVariantAttributes.getName(fluid))
                    .append(Component.literal(" x " + NumberUtils.getFormatedFluidBigNumber(amount) + " / "
                            + NumberUtils.getFormatedFluidBigNumber(handler.getSlotLimit(slot)))))
                    .translate(new Vec2(4, 4)));
        }
        if (any)
            tooltip.add(helper.box(contents, IBoxStyle.Empty.INSTANCE));
    }

    @Override
    public int getDefaultPriority() {
        return 4999;
    }

    @Override
    public ResourceLocation getUid() {
        return ID;
    }
}
