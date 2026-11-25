package com.koudesuk.functionalstorage.block;

import com.koudesuk.functionalstorage.block.tile.ArmoryCabinetTile;
import com.koudesuk.functionalstorage.registry.FSAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class ArmoryCabinetBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public ArmoryCabinetBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArmoryCabinetTile(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof ArmoryCabinetTile tile) {
            if (stack.has(FSAttachments.TILE)) {
                CompoundTag tileData = stack.get(FSAttachments.TILE);
                if (tileData != null) {
                    tile.loadFromTag(tileData, level.registryAccess());
                    tile.setChanged();
                }
            }
        }
    }

    // Right click opens the cabinet's screen, with or without an item in hand, like the original.
    // useItemOn keeps vanilla's PASS_TO_DEFAULT_BLOCK_INTERACTION, which comes here for a held item too
    @Override
    protected net.minecraft.world.InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            net.minecraft.world.entity.player.Player player, net.minecraft.world.phys.BlockHitResult hitResult) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof ArmoryCabinetTile tile)
            player.openMenu(tile);
        return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        ItemStack stack = new ItemStack(this);
        if (blockEntity instanceof ArmoryCabinetTile tile) {
            if (!tile.isEverythingEmpty() && tile.getLevel() != null) {
                stack.set(FSAttachments.TILE, tile.saveWithoutMetadata(tile.getLevel().registryAccess()));
            }
        }
        return Collections.singletonList(stack);
    }
}
