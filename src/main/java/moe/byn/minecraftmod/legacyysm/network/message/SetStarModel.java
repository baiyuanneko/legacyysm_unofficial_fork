package moe.byn.minecraftmod.legacyysm.network.message;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import moe.byn.minecraftmod.legacyysm.capability.YSMAttachments;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetStarModel(ResourceLocation modelId, boolean isAdd) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SetStarModel> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "set_star_model")
            );

    public static final StreamCodec<ByteBuf, SetStarModel> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, SetStarModel::modelId,
                    ByteBufCodecs.BOOL, SetStarModel::isAdd,
                    SetStarModel::new
            );

    public static SetStarModel add(ResourceLocation modelId) {
        return new SetStarModel(modelId, true);
    }

    public static SetStarModel remove(ResourceLocation modelId) {
        return new SetStarModel(modelId, false);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(final SetStarModel data, final IPayloadContext context) {
        handleCapability(data, context);
    }

    private static void handleCapability(SetStarModel data, IPayloadContext context) {
        var cap = context.player().getData(YSMAttachments.STAR_MODELS);
        if (data.isAdd) {
            cap.addModel(data.modelId);
        } else {
            cap.removeModel(data.modelId);
        }
    }
}
