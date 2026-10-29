package com.koudesuk.functionalstorage.compat.top;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.block.EnderDrawerBlock;
import com.koudesuk.functionalstorage.block.config.FunctionalStorageConfig;
import com.koudesuk.functionalstorage.block.tile.ArmoryCabinetTile;
import com.koudesuk.functionalstorage.block.tile.ControllableDrawerTile;
import com.koudesuk.functionalstorage.block.tile.ControllerExtensionTile;
import com.koudesuk.functionalstorage.block.tile.DrawerTile;
import com.koudesuk.functionalstorage.block.tile.EnderDrawerTile;
import com.koudesuk.functionalstorage.block.tile.FluidDrawerTile;
import com.koudesuk.functionalstorage.block.tile.ItemControllableDrawerTile;
import com.koudesuk.functionalstorage.block.tile.StorageControllerTile;
import com.koudesuk.functionalstorage.inventory.BigInventoryHandler;
import com.koudesuk.functionalstorage.inventory.CompactingInventoryHandler;
import com.koudesuk.functionalstorage.item.UpgradeItem;
import com.koudesuk.functionalstorage.util.NumberUtils;
import mcjty.theoneprobe.api.Color;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ITheOneProbe;
import mcjty.theoneprobe.api.NumberFormat;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.api.TankReference;
import mcjty.theoneprobe.api.TheOneProbePlugin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Locale;

/**
 * The One Probe support, a port of the original's TOPPlugin, FunctionalDrawerProvider and
 * FunctionalArmoryProvider, loaded through the "theoneprobe" entrypoint. The original drew each slot
 * with a custom element; this one lays out TOP's own item and text elements the same way, since a custom
 * element would need client classes in this common code.
 */
public class FSTOPPlugin implements TheOneProbePlugin {

    // The original's Color.CYAN.darker()
    private static final int BORDER = new Color(0, 178, 178).getRGB();
    // TOP's own chest contents panel, which the original replaces with the drawer's
    private static final String VERTICAL_ELEMENT = "mcjty.theoneprobe.apiimpl.elements.ElementVertical";

    @Override
    public void register(ITheOneProbe probe) {
        probe.registerProvider(new DrawerProvider());
        probe.registerProvider(new ArmoryProvider());
    }

    private static class DrawerProvider implements IProbeInfoProvider {
        @Override
        public Identifier getID() {
            return Identifier.fromNamespaceAndPath(FunctionalStorage.MOD_ID, "drawer");
        }

        @Override
        public void addProbeInfo(ProbeMode mode, IProbeInfo info, Player player, Level level, BlockState state,
                IProbeHitData data) {
            BlockEntity entity = level.getBlockEntity(data.getPos());
            if (!(entity instanceof ControllableDrawerTile<?> drawer) || entity instanceof StorageControllerTile
                    || entity instanceof ControllerExtensionTile)
                return;
            boolean extended = player.isShiftKeyDown() || mode == ProbeMode.EXTENDED;
            info.getElements().removeIf(element -> element.getClass().getName().equals(VERTICAL_ELEMENT));
            IProbeInfo vertical = info.vertical();
            if (entity instanceof DrawerTile drawerTile)
                addItems(info, vertical, drawerTile.getHandler(),
                        drawerTile.getHandler().getStoredStacks().size() == 1 || extended);
            if (entity instanceof EnderDrawerTile ender) {
                addItems(info, vertical, ender.getHandler(), extended);
                IProbeInfo frequency = vertical.vertical(box(info).leftPadding(0).rightPadding(0));
                frequency.text(Component.translatable("linkingtool.ender.frequency"));
                IProbeInfo icons = frequency.horizontal(info.defaultLayoutStyle().copy().leftPadding(4).topPadding(2)
                        .rightPadding(4));
                for (ItemStack stack : EnderDrawerBlock.getFrequencyDisplay(ender.getFrequency()))
                    icons.item(stack);
            }
            if (entity instanceof ItemControllableDrawerTile<?> itemDrawer
                    && itemDrawer.getStorage() instanceof CompactingInventoryHandler compacting)
                addCompacting(info, vertical, compacting, extended || compacting.isCreative());
            if (entity instanceof FluidDrawerTile fluidDrawer && extended) {
                IProbeInfo tanks = info.vertical(info.defaultLayoutStyle().copy().spacing(2));
                for (TankReference tank : TankReference.createSplitHandler(fluidDrawer.getHandler()))
                    tanks.tank(tank, info.defaultProgressStyle().copy().numberFormat(NumberFormat.COMPACT));
            }
            if (extended)
                addUpgrades(info, vertical, drawer);
        }

        private static void addItems(IProbeInfo info, IProbeInfo vertical, BigInventoryHandler handler,
                boolean rows) {
            IProbeInfo panel = rows ? vertical.vertical(box(info)) : vertical.horizontal(box(info).spacing(8));
            for (int i = 0; i < handler.getStoredStacks().size(); i++) {
                BigInventoryHandler.BigStack stored = handler.getStoredStacks().get(i);
                if (stored.getAmount() > 0 || handler.isLocked() && !stored.getStack().isEmpty())
                    slot(info, panel, stored.getStack(), NumberUtils.getFormatedBigNumber(stored.getAmount()) + "/"
                            + NumberUtils.getFormatedBigNumber(handler.getSlotLimit(i)), rows);
            }
        }

        private static void addCompacting(IProbeInfo info, IProbeInfo vertical, CompactingInventoryHandler handler,
                boolean rows) {
            var results = handler.getResultList();
            if (rows) {
                IProbeInfo panel = vertical.vertical(box(info));
                for (int slot = 0; slot < results.size(); slot++) {
                    if (!results.get(slot).getResult().isEmpty())
                        slot(info, panel, results.get(slot).getResult(),
                                NumberUtils.getFormatedBigNumber(handler.getStackInSlot(slot).getCount()) + "/"
                                        + NumberUtils.getFormatedBigNumber(handler.getSlotLimit(slot)), true);
                }
                return;
            }
            // Like the original, the amount spread over the tiers instead of each tier's total
            IProbeInfo panel = vertical.horizontal(box(info).spacing(8));
            int amount = handler.getAmount();
            int[] displayed = new int[results.size()];
            for (int slot = displayed.length - 1; slot >= 0; slot--) {
                int needed = Math.max(1, results.get(slot).getNeeded());
                displayed[slot] = amount / needed;
                amount -= needed * displayed[slot];
            }
            for (int slot = 0; slot < displayed.length; slot++) {
                if (!results.get(slot).getResult().isEmpty())
                    slot(info, panel, results.get(slot).getResult(), NumberUtils.getFormatedBigNumber(displayed[slot]),
                            false);
            }
        }

        private static void addUpgrades(IProbeInfo info, IProbeInfo vertical, ControllableDrawerTile<?> drawer) {
            IProbeInfo panel = null;
            for (int i = 0; i < drawer.getUtilityUpgrades().getContainerSize(); i++) {
                ItemStack stack = drawer.getUtilityUpgrades().getItem(i);
                if (stack.isEmpty())
                    continue;
                if (panel == null)
                    panel = vertical.horizontal(box(info).spacing(8));
                String extra = UpgradeItem.isDirectionUpgrade(stack.getItem())
                        ? capitalize(UpgradeItem.getDirection(stack).getName())
                        : "";
                slot(info, panel, stack, extra, false);
            }
        }

        /**
         * One slot: the item, then its amount and, in rows, its name.
         */
        private static void slot(IProbeInfo info, IProbeInfo panel, ItemStack stack, String text, boolean name) {
            IProbeInfo row = panel.horizontal(info.defaultLayoutStyle().copy().alignment(ElementAlignment.ALIGN_CENTER));
            row.item(stack.copyWithCount(1));
            if (name)
                row.itemLabel(stack);
            if (!text.isEmpty())
                row.text(Component.literal(text));
        }

        private static mcjty.theoneprobe.api.ILayoutStyle box(IProbeInfo info) {
            return info.defaultLayoutStyle().copy().spacing(2).leftPadding(7).rightPadding(7).borderColor(BORDER);
        }

        private static String capitalize(String name) {
            return name.isEmpty() ? name : name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1);
        }
    }

    private static class ArmoryProvider implements IProbeInfoProvider {
        @Override
        public Identifier getID() {
            return Identifier.fromNamespaceAndPath(FunctionalStorage.MOD_ID, "armory");
        }

        @Override
        public void addProbeInfo(ProbeMode mode, IProbeInfo info, Player player, Level level, BlockState state,
                IProbeHitData data) {
            if (level.getBlockEntity(data.getPos()) instanceof ArmoryCabinetTile armory) {
                int count = 0;
                for (int slot = 0; slot < armory.handler.getContainerSize(); slot++) {
                    if (!armory.handler.getItem(slot).isEmpty())
                        count++;
                }
                info.vertical().mcText(Component.literal(count + " / " + FunctionalStorageConfig.ARMORY_CABINET_SIZE.get()));
            }
        }
    }
}
