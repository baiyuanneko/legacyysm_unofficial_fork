package moe.byn.minecraftmod.legacyysm.network.message;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class RequestSyncModel implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RequestSyncModel> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "request_sync_model"));

    public static final StreamCodec<ByteBuf, RequestSyncModel> STREAM_CODEC = StreamCodec.of(
            RequestSyncModel::encode,
            RequestSyncModel::decode
    );
    
    private static void encode(ByteBuf buf, RequestSyncModel msg) {
        buf.writeBoolean(msg.allowModelSync);
    }
    
    private static RequestSyncModel decode(ByteBuf buf) {
        return new RequestSyncModel(buf.readBoolean());
    }

    private final boolean allowModelSync;

    public RequestSyncModel(boolean allowModelSync) {
        this.allowModelSync = allowModelSync;
    }
    
    public boolean isAllowModelSync() {
        return allowModelSync;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(RequestSyncModel message, IPayloadContext context) {
        context.enqueueWork(() -> {
            try {
                Class<?> clientModelManagerClass = Class.forName("moe.byn.minecraftmod.legacyysm.client.ClientModelManager");
                
                java.lang.reflect.Field field = clientModelManagerClass.getDeclaredField("SERVER_ALLOWS_MODEL_SYNC");
                field.set(null, message.allowModelSync);
                YesSteveModel.LOGGER.info("Server config received via RequestSyncModel: allowModelSync={}", message.allowModelSync);
                
                Class<?> helperClass = Class.forName("moe.byn.minecraftmod.legacyysm.client.ClientModelSyncHelper");
                java.lang.reflect.Method method = helperClass.getMethod("sendSyncModelMessage");
                method.invoke(null);
            } catch (Exception e) {
                YesSteveModel.LOGGER.error("Failed to handle RequestSyncModel", e);
            }
        });
    }
}
