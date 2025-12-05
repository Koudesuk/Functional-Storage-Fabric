package com.koudesuk.functionalstorage.mixin.client;

import com.koudesuk.functionalstorage.block.FramedCompactingDrawerBlock;
import com.koudesuk.functionalstorage.block.FramedDrawerBlock;
import com.koudesuk.functionalstorage.block.FramedDrawerControllerBlock;
import com.koudesuk.functionalstorage.block.FramedSimpleCompactingDrawerBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to intercept ItemRenderer.getModel() for framed drawer items.
 * 
 * When a framed drawer item has custom texture data (stored in NBT "Style"
 * tag),
 * this mixin returns the block model (FramedDrawerBakedModel) instead of the
 * item model.
 * 
 * The FramedDrawerBakedModel.emitItemQuads() method will then be called with
 * the ItemStack,
 * allowing it to read the NBT and apply custom textures.
 */
@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    /**
     * Intercepts getModel to return custom block model for framed drawer items with
     * NBT textures.
     */
    @Inject(method = "getModel", at = @At("HEAD"), cancellable = true)
    private void functionalstorage$getFramedDrawerModel(ItemStack stack, Level world, LivingEntity entity, int seed,
            CallbackInfoReturnable<BakedModel> cir) {

        // Only process BlockItems
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return;
        }

        Block block = blockItem.getBlock();

        // Check if this is a framed drawer type block
        if (!isFramedDrawerBlock(block)) {
            return;
        }

        // Check if the item has custom texture data in NBT
        if (!hasCustomTextureData(stack)) {
            return;
        }

        // Get the block model (which is our FramedDrawerBakedModel)
        BakedModel blockModel = Minecraft.getInstance()
                .getBlockRenderer()
                .getBlockModel(block.defaultBlockState());

        // Return the block model so emitItemQuads() will be called with the ItemStack
        cir.setReturnValue(blockModel);
    }

    /**
     * Checks if the given block is a framed drawer type.
     */
    private boolean isFramedDrawerBlock(Block block) {
        return block instanceof FramedDrawerBlock
                || block instanceof FramedCompactingDrawerBlock
                || block instanceof FramedSimpleCompactingDrawerBlock
                || block instanceof FramedDrawerControllerBlock;
    }

    /**
     * Checks if the ItemStack has custom texture data in the "Style" NBT tag.
     */
    private boolean hasCustomTextureData(ItemStack stack) {
        if (!stack.hasTag()) {
            return false;
        }
        return stack.getTag().contains("Style");
    }
}
