package com.koudesuk.functionalstorage.item;

import com.koudesuk.functionalstorage.block.DrawerFront;
import com.koudesuk.functionalstorage.block.FluidDrawerBlock;
import com.koudesuk.functionalstorage.block.FramedBlock;
import com.koudesuk.functionalstorage.block.tile.ControllableDrawerTile;
import com.koudesuk.functionalstorage.block.tile.DrawerTile;
import com.koudesuk.functionalstorage.block.tile.FluidDrawerTile;
import com.koudesuk.functionalstorage.registry.FSAttachments;
import com.koudesuk.functionalstorage.util.NumberUtils;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public class DrawerBlockItem extends BlockItem {

    public DrawerBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
            Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);

        // The original's contents lines. Item drawers draw their "Contents:" line together with the
        // item icons (getTooltipImage), which vanilla always places right below the name.
        HolderLookup.Provider registries = context.registries();
        ControllableDrawerTile<?> tile = registries != null ? loadTile(stack, registries) : null;
        if (tile instanceof FluidDrawerTile fluidTile) {
            appendFluids(fluidTile, tooltip);
        }
        if (tile instanceof DrawerTile || tile instanceof FluidDrawerTile) {
            appendUpgrades(tile, tooltip);
        }

        if (getBlock() instanceof FramedBlock) {
            FramedBlock.appendTooltip(tooltip);
        }
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        // Stored items show as icons like the original's tooltip image; fluid drawers list theirs as text
        if (getBlock() instanceof DrawerFront && !(getBlock() instanceof FluidDrawerBlock) && tileTag(stack) != null) {
            return Optional.of(new DrawerContentsTooltip(stack));
        }
        return Optional.empty();
    }

    /**
     * Loads the drawer this item carries into a tile that is never placed, so its contents read
     * through the same handlers as the placed drawer. Null for items without drawer data.
     */
    @Nullable
    public ControllableDrawerTile<?> loadTile(ItemStack stack, HolderLookup.Provider registries) {
        return tileTag(stack) == null ? null : createTile(stack, registries);
    }

    /**
     * Like loadTile, but an item without drawer data gives an empty drawer, for DrawerItemStorage.
     */
    @Nullable
    public ControllableDrawerTile<?> createTile(ItemStack stack, HolderLookup.Provider registries) {
        if (!(getBlock() instanceof DrawerFront) || !(getBlock() instanceof EntityBlock block)
                || !(block.newBlockEntity(BlockPos.ZERO, getBlock().defaultBlockState())
                        instanceof ControllableDrawerTile<?> tile)) {
            return null;
        }
        CompoundTag tag = tileTag(stack);
        if (tag != null)
            tile.loadFromTag(tag, registries);
        return tile;
    }

    // Broken drawers keep their tile in FSAttachments.TILE; a creative pick with data uses the
    // vanilla block entity component
    @Nullable
    private static CompoundTag tileTag(ItemStack stack) {
        CompoundTag tag = stack.get(FSAttachments.TILE);
        if (tag != null) {
            return tag;
        }
        TypedEntityData<BlockEntityType<?>> data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        return data != null ? data.copyTagWithoutId() : null;
    }

    private static void appendFluids(FluidDrawerTile tile, Consumer<Component> tooltip) {
        tooltip.accept(Component.translatable("drawer.block.contents").withStyle(ChatFormatting.GRAY));
        for (int slot = 0; slot < tile.getDrawerType().getSlots(); slot++) {
            FluidVariant fluid = tile.getHandler().getResource(slot);
            long amount = tile.getHandler().getAmount(slot);
            if (!fluid.isBlank() && amount > 0) {
                tooltip.accept(Component.literal(" - " + ChatFormatting.YELLOW + NumberUtils.getFormatedFluidBigNumber(amount)
                        + ChatFormatting.WHITE + " of ")
                        .append(FluidVariantAttributes.getName(fluid).copy().withStyle(ChatFormatting.GOLD)));
            }
        }
    }

    // Like the original, only the upgrades that change what the drawer does to items are listed
    private static void appendUpgrades(ControllableDrawerTile<?> tile, Consumer<Component> tooltip) {
        tooltip.accept(Component.translatable("drawer.block.upgrades").withStyle(ChatFormatting.GRAY));
        if (tile.isCreative()) {
            tooltip.accept(upgradeLine("drawer.block.upgrades.is_creative", ChatFormatting.LIGHT_PURPLE));
        }
        if (tile.isVoid()) {
            tooltip.accept(upgradeLine("drawer.block.upgrades.is_void", ChatFormatting.BLUE));
        }
        if (!tile.isCreative() && !tile.isVoid()) {
            tooltip.accept(upgradeLine("drawer.block.upgrades.none", ChatFormatting.GRAY));
        }
    }

    private static Component upgradeLine(String key, ChatFormatting color) {
        return Component.literal("- ").withStyle(ChatFormatting.GRAY)
                .append(Component.translatable(key).withStyle(color));
    }
}
