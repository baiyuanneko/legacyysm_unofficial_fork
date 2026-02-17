package moe.byn.minecraftmod.legacyysm.network.message;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import moe.byn.minecraftmod.legacyysm.config.ServerConfig;
import moe.byn.minecraftmod.legacyysm.model.PlayerModelCache;
import moe.byn.minecraftmod.legacyysm.util.ModelIdValidator;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class UploadPlayerModel implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UploadPlayerModel> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "upload_player_model"));

    public static final StreamCodec<ByteBuf, UploadPlayerModel> STREAM_CODEC = StreamCodec.of(
            UploadPlayerModel::encode,
            UploadPlayerModel::decode
    );

    private static void encode(ByteBuf buf, UploadPlayerModel msg) {
        FriendlyByteBuf friendlyBuf = new FriendlyByteBuf(buf);
        friendlyBuf.writeUtf(msg.modelId);
        friendlyBuf.writeByteArray(msg.modelData);
    }

    private static UploadPlayerModel decode(ByteBuf buf) {
        FriendlyByteBuf friendlyBuf = new FriendlyByteBuf(buf);
        String modelId = friendlyBuf.readUtf();
        byte[] modelData = friendlyBuf.readByteArray();
        return new UploadPlayerModel(modelId, modelData);
    }

    private final String modelId;
    private final byte[] modelData;

    public UploadPlayerModel(String modelId, byte[] modelData) {
        this.modelId = modelId;
        this.modelData = modelData;
    }

    public String getModelId() {
        return modelId;
    }

    public byte[] getModelData() {
        return modelData;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(UploadPlayerModel message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            
            if (!ServerConfig.ALLOW_MODEL_SYNC.get()) {
                YesSteveModel.LOGGER.warn("Player {} tried to upload model {} but client upload is disabled (allowModelSync=false)", 
                        serverPlayer.getName().getString(), message.modelId);
                return;
            }
            
            if (!ModelIdValidator.isValidModelId(message.modelId)) {
                YesSteveModel.LOGGER.warn("Player {} tried to upload model with invalid modelId: {}", 
                        serverPlayer.getName().getString(), message.modelId);
                return;
            }
            
            int maxSize = ServerConfig.MAX_MODEL_SIZE_BYTES.get();
            if (message.modelData.length > maxSize) {
                YesSteveModel.LOGGER.warn("Player {} tried to upload model {} that exceeds size limit ({} > {})",
                        serverPlayer.getName().getString(), message.modelId, message.modelData.length, maxSize);
                return;
            }

            boolean success = PlayerModelCache.cachePlayerModel(serverPlayer, message.modelId, message.modelData);
            if (success) {
                YesSteveModel.LOGGER.info("Player {} uploaded model {} ({} bytes)",
                        serverPlayer.getName().getString(), message.modelId, message.modelData.length);
                
                PlayerModelCache.broadcastPlayerModel(serverPlayer, message.modelId, message.modelData);
            }
        });
    }
}
