package com.koudesuk.functionalstorage.item.component;

import com.koudesuk.functionalstorage.block.tile.ControllableDrawerTile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

import java.util.List;

/**
 * Puts fluid into the drawer it is installed in, like the original's Dripping and Water Generator Upgrades.
 * Amounts are in millibuckets, as in the original's data.
 */
public record GenerateFluidBehavior(FluidStackTemplate fluid) implements FunctionalUpgradeBehavior {
    public static final MapCodec<GenerateFluidBehavior> CODEC = RecordCodecBuilder.mapCodec(in -> in.group(
            FluidStackTemplate.CODEC.fieldOf("fluid").forGetter(GenerateFluidBehavior::fluid))
            .apply(in, GenerateFluidBehavior::new));

    @Override
    public void work(Level level, BlockPos pos, ControllableDrawerTile<?> drawer, ItemStack upgradeStack,
            int upgradeSlot) {
        Storage<FluidVariant> storage = FluidStorage.SIDED.find(level, pos, Direction.UP);
        if (storage != null) {
            try (Transaction transaction = Transaction.openOuter()) {
                storage.insert(fluid.variant(), fluid.droplets(), transaction);
                transaction.commit();
            }
        }
    }

    @Override
    public MapCodec<? extends FunctionalUpgradeBehavior> codec() {
        return CODEC;
    }

    @Override
    public List<Component> getTooltip() {
        var list = FunctionalUpgradeBehavior.super.getTooltip();
        list.add(Component.translatable("functionalupgrade.desc.generate_fluid", fluid.amount(),
                FluidVariantAttributes.getName(fluid.variant()).getString()));
        return list;
    }

    /**
     * A fluid and an amount in millibuckets, with the same fields as NeoForge's FluidStackTemplate.
     */
    public record FluidStackTemplate(Holder<Fluid> fluid, int amount) {
        public static final Codec<FluidStackTemplate> CODEC = RecordCodecBuilder.create(in -> in.group(
                BuiltInRegistries.FLUID.holderByNameCodec().fieldOf("id").forGetter(FluidStackTemplate::fluid),
                Codec.intRange(1, Integer.MAX_VALUE).fieldOf("amount").forGetter(FluidStackTemplate::amount))
                .apply(in, FluidStackTemplate::new));

        public FluidStackTemplate(Fluid fluid, int amount) {
            this(fluid.builtInRegistryHolder(), amount);
        }

        public FluidVariant variant() {
            return FluidVariant.of(fluid.value());
        }

        public long droplets() {
            return amount * (FluidConstants.BUCKET / 1000);
        }
    }
}
