package com.koudesuk.functionalstorage.client;

import com.koudesuk.functionalstorage.block.DrawerFront;
import com.koudesuk.functionalstorage.network.DrawerClickPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Left clicks on drawer fronts. While the attack key is held the game attacks the block
 * again every tick, and each attack used to pull another item out, so a click took two or
 * three. Now a press sends one DrawerClickPayload with the crosshair hit and nothing more
 * is sent until the key is released.
 */
public final class DrawerClickHandler {

    private static boolean armed = true;

    private DrawerClickHandler() {
    }

    public static void register() {
        AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) -> {
            // A server without DrawerClickPayload (older version) still handles the vanilla attack
            if (!level.isClientSide() || !DrawerFront.isFront(level.getBlockState(pos), direction)
                    || !ClientPlayNetworking.canSend(DrawerClickPayload.TYPE))
                return InteractionResult.PASS;
            if (armed && Minecraft.getInstance().hitResult instanceof BlockHitResult hit
                    && hit.getBlockPos().equals(pos)) {
                armed = false;
                ClientPlayNetworking.send(new DrawerClickPayload(hit));
            }
            // FAIL cancels the attack without telling the server, so the front is never mined or broken
            return InteractionResult.FAIL;
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!client.options.keyAttack.isDown())
                armed = true;
        });
    }
}
