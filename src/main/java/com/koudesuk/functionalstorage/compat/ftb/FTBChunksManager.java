package com.koudesuk.functionalstorage.compat.ftb;

import com.koudesuk.functionalstorage.FunctionalStorage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Method;

/**
 * FTB Chunks claim protection for taking items out of a drawer, like the original's FTBChunksManager.
 * Players take items through DrawerClickPayload, which FTB Chunks' own block events never see. FTB Chunks
 * is optional, so its API is reached by reflection, the way the original loads this class only when the
 * mod is present; nothing of it is needed to build this mod.
 * <p>
 * FTB Chunks 2001.x for 1.20.1: {@code dev.ftb.mods.ftbchunks.data.ClaimedChunkManagerImpl.getInstance()}
 * is null until a server started, and
 * {@code shouldPreventInteraction(Entity, InteractionHand, BlockPos, dev.ftb.mods.ftbchunks.api.Protection, Entity)}
 * answers the claim check.
 */
public final class FTBChunksManager {
    private static final boolean LOADED = FabricLoader.getInstance().isModLoaded("ftbchunks");

    private static Method getInstance;
    private static Method shouldPreventInteraction;
    private static Object interactBlock;
    private static boolean failed;

    private FTBChunksManager() {
    }

    public static boolean preventInteraction(BlockPos pos, Player player) {
        if (!LOADED || failed)
            return false;
        try {
            if (shouldPreventInteraction == null)
                lookUp();
            Object manager = getInstance.invoke(null);
            if (manager == null)
                return false;
            return (boolean) shouldPreventInteraction.invoke(manager, player, InteractionHand.MAIN_HAND, pos,
                    interactBlock, null)
                    || (boolean) shouldPreventInteraction.invoke(manager, player, InteractionHand.OFF_HAND, pos,
                            interactBlock, null);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            failed = true;
            FunctionalStorage.LOGGER.warn("FTB Chunks protection check failed, drawers will not check claims", e);
            return false;
        }
    }

    private static void lookUp() throws ReflectiveOperationException {
        Class<?> manager = Class.forName("dev.ftb.mods.ftbchunks.data.ClaimedChunkManagerImpl");
        Class<?> protection = Class.forName("dev.ftb.mods.ftbchunks.api.Protection");
        getInstance = manager.getMethod("getInstance");
        interactBlock = protection.getField("INTERACT_BLOCK").get(null);
        shouldPreventInteraction = manager.getMethod("shouldPreventInteraction", Entity.class, InteractionHand.class,
                BlockPos.class, protection, Entity.class);
    }
}
