package com.koudesuk.functionalstorage.client.model;

import com.koudesuk.functionalstorage.block.FramedBlock;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;

/**
 * Wraps the baked model of every framed block state in FramedDrawerBakedModel, like the
 * original's FramedModel.wrapModels on NeoForge's ModifyBakingResult.
 */
public class FramedDrawerModelLoader implements ModelLoadingPlugin {

    @Override
    public void initialize(Context pluginContext) {
        // This plugin is both the fabric-model-loading entrypoint and registered by
        // FunctionalStorageClient, so a model can arrive already wrapped
        pluginContext.modifyBlockModelAfterBake().register((model, context) ->
                context.state().getBlock() instanceof FramedBlock && !(model instanceof FramedDrawerBakedModel)
                        ? new FramedDrawerBakedModel(model)
                        : model);
    }
}
