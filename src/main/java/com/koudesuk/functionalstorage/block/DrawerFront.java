package com.koudesuk.functionalstorage.block;

import com.koudesuk.functionalstorage.block.tile.ControllableDrawerTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

/**
 * A block whose front face is split into clickable drawer slots.
 */
public interface DrawerFront {

    /**
     * The slot under the hit, or -1 for the frame, the dividers and every other face.
     */
    int getHit(BlockState state, BlockPos pos, BlockHitResult hit);

    static boolean isFront(BlockState state, Direction side) {
        return state.getBlock() instanceof DrawerFront
                && state.getValue(BlockStateProperties.HORIZONTAL_FACING) == side;
    }

    /**
     * Mining makes no progress while the player looks at the front. Without this, holding the
     * button on the front resumes a break the player started (and let go of) on another face.
     * Only the client is asked: its view is exact and it decides when the block is broken, while
     * the server's copy of the view lags a tick and would spoil legitimate mining from the side.
     */
    static boolean isMiningFront(BlockState state, Player player, BlockPos pos) {
        return player.level().isClientSide()
                && player.pick(20, 1.0F, false) instanceof BlockHitResult hit
                && hit.getBlockPos().equals(pos)
                && isFront(state, hit.getDirection());
    }

    /**
     * Left click on a drawer front. Players send their crosshair hit through
     * DrawerClickPayload; automation (deployer fake players) comes through
     * AttackBlockCallback with a server side ray trace.
     */
    static void click(ServerPlayer player, BlockHitResult hit) {
        BlockPos pos = hit.getBlockPos();
        Level level = player.level();
        if (!level.isLoaded(pos) || !player.canInteractWithBlock(pos, 1.0)
                || player.blockActionRestricted(level, pos, player.gameMode.getGameModeForPlayer()))
            return;
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof DrawerFront drawer
                && level.getBlockEntity(pos) instanceof ControllableDrawerTile<?> tile) {
            int slot = drawer.getHit(state, pos, hit);
            if (slot != -1)
                tile.onClicked(player, slot);
        }
    }
}
