package moe.byn.minecraftmod.legacyysm.network.message;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import moe.byn.minecraftmod.legacyysm.capability.YSMAttachments;
import moe.byn.minecraftmod.legacyysm.config.ServerConfig;
import moe.byn.minecraftmod.legacyysm.model.ServerModelManager;
import moe.byn.minecraftmod.legacyysm.model.PlayerModelCache;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetModelAndTexture(ResourceLocation modelId, ResourceLocation selectTexture) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SetModelAndTexture> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "set_model_and_texture")
            );

    public static final StreamCodec<ByteBuf, SetModelAndTexture> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, SetModelAndTexture::modelId,
                    ResourceLocation.STREAM_CODEC, SetModelAndTexture::selectTexture,
                    SetModelAndTexture::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(final SetModelAndTexture data, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            
            String modelPath = data.modelId.getPath();
            
            boolean isServerModel = ServerModelManager.CACHE_NAME_INFO.containsKey(modelPath);
            boolean isPlayerUploadedModel = PlayerModelCache.hasCachedModel(serverPlayer.getUUID(), modelPath);
            boolean isAuthModel = ServerModelManager.AUTH_MODELS.contains(modelPath);
            
            if (isServerModel || isPlayerUploadedModel) {
                if (isAuthModel) {
                    var ownModelsCap = serverPlayer.getData(YSMAttachments.AUTH_MODELS);
                    if (!ownModelsCap.containModel(data.modelId)) {
                        YesSteveModel.LOGGER.debug("Player {} tried to use auth model {} without permission",
                                serverPlayer.getName().getString(), modelPath);
                        return;
                    }
                }
                
                var modelIdCap = serverPlayer.getData(YSMAttachments.MODEL_INFO);
                modelIdCap.setModelAndTexture(data.modelId, data.selectTexture);
                
                YesSteveModel.LOGGER.debug("Player {} set model to {}", serverPlayer.getName().getString(), modelPath);
            } else if (ServerConfig.ALLOW_MODEL_SYNC.get()) {
                YesSteveModel.LOGGER.debug("Player {} requested local model {}, waiting for upload", 
                        serverPlayer.getName().getString(), modelPath);
            } else {
                YesSteveModel.LOGGER.warn("Player {} tried to use unknown model {} and model sync is disabled",
                        serverPlayer.getName().getString(), modelPath);
            }
        });
    }
}
