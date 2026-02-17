package moe.byn.minecraftmod.legacyysm.network.message;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SyncServerConfig implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncServerConfig> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "sync_server_config"));

    public static final StreamCodec<ByteBuf, SyncServerConfig> STREAM_CODEC = StreamCodec.of(
            SyncServerConfig::encode,
            SyncServerConfig::decode
    );

    private static void encode(ByteBuf buf, SyncServerConfig msg) {
        buf.writeBoolean(msg.allowModelSync);
    }

    private static SyncServerConfig decode(ByteBuf buf) {
        return new SyncServerConfig(buf.readBoolean());
    }

    private final boolean allowModelSync;

    public SyncServerConfig(boolean allowModelSync) {
        this.allowModelSync = allowModelSync;
    }

    public boolean isAllowModelSync() {
        return allowModelSync;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(SyncServerConfig message, IPayloadContext context) {
        context.enqueueWork(() -> {
            try {
                Class<?> clientModelManagerClass = Class.forName("moe.byn.minecraftmod.legacyysm.client.ClientModelManager");
                
                java.lang.reflect.Field field = clientModelManagerClass.getDeclaredField("SERVER_ALLOWS_MODEL_SYNC");
                field.set(null, message.allowModelSync);
                
                if (message.allowModelSync) {
                    java.lang.reflect.Method reloadMethod = clientModelManagerClass.getMethod("reloadLocalModels");
                    reloadMethod.invoke(null);
                    YesSteveModel.LOGGER.info("Server allows model sync, reloaded local models");
                }
                
                YesSteveModel.LOGGER.info("Server config synced: allowModelSync={}", message.allowModelSync);
            } catch (Exception e) {
                YesSteveModel.LOGGER.error("Failed to sync server config", e);
            }
        });
    }
}
