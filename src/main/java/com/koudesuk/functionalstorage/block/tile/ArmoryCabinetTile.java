package com.koudesuk.functionalstorage.block.tile;

import com.koudesuk.functionalstorage.inventory.ArmoryCabinetInventoryHandler;
import com.koudesuk.functionalstorage.inventory.ArmoryCabinetMenu;
import com.koudesuk.functionalstorage.registry.FSAttachments;
import com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import org.jetbrains.annotations.Nullable;

public class ArmoryCabinetTile extends BlockEntity
        implements ExtendedMenuProvider<ArmoryCabinetMenu.OpenData> {

    public ArmoryCabinetInventoryHandler handler;

    public ArmoryCabinetTile(BlockPos pos, BlockState state) {
        super(FunctionalStorageBlockEntities.ARMORY_CABINET, pos, state);
        this.handler = new ArmoryCabinetInventoryHandler() {
            @Override
            public void onChange() {
                ArmoryCabinetTile.this.setChanged();
            }
        };
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        input.read("handler", CompoundTag.CODEC).ifPresent(tag -> this.handler.deserializeNBT(tag, input.lookup()));
    }

    /**
     * Public method for loading tile data from an ItemStack's component data.
     * This is used in Block.setPlacedBy() to restore tile state.
     */
    public void loadFromTag(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        this.loadAdditional(TagValueInput.create(ProblemReporter.DISCARDING, registries, tag));
    }

    /**
     * The block's setPlacedBy reads TILE. Getting it here only marks it as used, so vanilla does not
     * also keep it in the saved components, which nested it into every later drop.
     */
    @Override
    protected void applyImplicitComponents(net.minecraft.core.component.DataComponentGetter components) {
        super.applyImplicitComponents(components);
        components.get(FSAttachments.TILE);
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);
        if (this.level != null) {
            output.store("handler", CompoundTag.CODEC, this.handler.serializeNBT(this.level.registryAccess()));
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    public boolean isEverythingEmpty() {
        return handler.isEmpty();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.functionalstorage.armory_cabinet");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ArmoryCabinetMenu(id, inventory, handler, getBlockPos());
    }

    // The client gets the contents when the screen opens, like the original
    @Override
    public ArmoryCabinetMenu.OpenData getScreenOpeningData(ServerPlayer player) {
        return new ArmoryCabinetMenu.OpenData(getBlockPos(), handler.serializeNBT(player.level().registryAccess()));
    }
}
