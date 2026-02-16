package moe.byn.minecraftmod.legacyysm.network.message;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class RequestSyncModel implements CustomPacketPayload {
    public static final RequestSyncModel INSTANCE = new RequestSyncModel();
    
    public static final CustomPacketPayload.Type<RequestSyncModel> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "request_sync_model"));

    public static final StreamCodec<ByteBuf, RequestSyncModel> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    public RequestSyncModel() {
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(RequestSyncModel message, IPayloadContext context) {
        context.enqueueWork(() -> {
            try {
                Class<?> helperClass = Class.forName("moe.byn.minecraftmod.legacyysm.client.ClientModelSyncHelper");
                java.lang.reflect.Method method = helperClass.getMethod("sendSyncModelMessage");
                method.invoke(null);
            } catch (Exception e) {
                YesSteveModel.LOGGER.error("Failed to send sync model message", e);
            }
        });
    }
}
