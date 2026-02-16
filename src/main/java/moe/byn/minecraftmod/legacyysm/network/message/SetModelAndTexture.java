package moe.byn.minecraftmod.legacyysm.network.message;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import moe.byn.minecraftmod.legacyysm.capability.YSMAttachments;
import moe.byn.minecraftmod.legacyysm.model.ServerModelManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
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
        handleCapability(data, context);
    }

    private static void handleCapability(SetModelAndTexture data, IPayloadContext context) {
        var modelIdCap = context.player().getData(YSMAttachments.MODEL_INFO);
        var ownModelsCap = context.player().getData(YSMAttachments.AUTH_MODELS);
        if (!ServerModelManager.AUTH_MODELS.contains(data.modelId.getPath()) || ownModelsCap.containModel(data.modelId)) {
            modelIdCap.setModelAndTexture(data.modelId, data.selectTexture);
        }
    }
}
