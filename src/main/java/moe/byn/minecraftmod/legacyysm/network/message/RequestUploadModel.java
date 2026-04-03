package moe.byn.minecraftmod.legacyysm.network.message;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import moe.byn.minecraftmod.legacyysm.client.ClientModelManager;
import moe.byn.minecraftmod.legacyysm.network.NetworkHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class RequestUploadModel implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RequestUploadModel> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "request_upload_model"));

    public static final StreamCodec<ByteBuf, RequestUploadModel> STREAM_CODEC = StreamCodec.of(
            RequestUploadModel::encode,
            RequestUploadModel::decode
    );

    private static void encode(ByteBuf buf, RequestUploadModel msg) {
        FriendlyByteBuf friendlyBuf = new FriendlyByteBuf(buf);
        friendlyBuf.writeUtf(msg.modelId);
    }

    private static RequestUploadModel decode(ByteBuf buf) {
        FriendlyByteBuf friendlyBuf = new FriendlyByteBuf(buf);
        return new RequestUploadModel(friendlyBuf.readUtf());
    }

    private final String modelId;

    public RequestUploadModel(String modelId) {
        this.modelId = modelId;
    }

    public String getModelId() {
        return modelId;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(RequestUploadModel message, IPayloadContext context) {
        context.enqueueWork(() -> {
            YesSteveModel.LOGGER.info("Server requested upload of model: {}", message.modelId);

            byte[] modelData = ClientModelManager.getLocalModelData(message.modelId);
            if (modelData != null) {
                NetworkHandler.sendToServer(new UploadPlayerModel(message.modelId, modelData));
                YesSteveModel.LOGGER.info("Auto-uploaded model {} ({} bytes) on server request",
                        message.modelId, modelData.length);
            } else {
                YesSteveModel.LOGGER.warn("Server requested model {} but local data not found", message.modelId);
            }
        });
    }
}
