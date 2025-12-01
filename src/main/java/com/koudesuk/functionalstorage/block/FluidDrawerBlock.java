package com.koudesuk.functionalstorage.block;

import com.koudesuk.functionalstorage.block.tile.FluidDrawerTile;
import com.koudesuk.functionalstorage.util.DrawerType;
import com.koudesuk.functionalstorage.util.DrawerWoodType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class FluidDrawerBlock extends DrawerBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public FluidDrawerBlock(DrawerType type, Properties properties) {
        super(DrawerWoodType.OAK, type, properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FluidDrawerTile(pos, state, this.getType());
    }
}
