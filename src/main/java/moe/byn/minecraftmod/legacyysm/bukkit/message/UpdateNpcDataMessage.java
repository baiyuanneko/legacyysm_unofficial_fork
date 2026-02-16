package moe.byn.minecraftmod.legacyysm.bukkit.message;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public class UpdateNpcDataMessage implements CustomPacketPayload {
    public static final Type<UpdateNpcDataMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("legacyysm_byn", "update_npc_data"));
    
    public static final StreamCodec<ByteBuf, UpdateNpcDataMessage> STREAM_CODEC = StreamCodec.of(
            UpdateNpcDataMessage::encode,
            UpdateNpcDataMessage::decode
    );
    
    private final UUID uuid;
    private final ResourceLocation modelId;
    private final ResourceLocation textureId;

    public UpdateNpcDataMessage(UUID uuid, ResourceLocation modelId, ResourceLocation textureId) {
        this.uuid = uuid;
        this.modelId = modelId;
        this.textureId = textureId;
    }

    private static void encode(ByteBuf buf, UpdateNpcDataMessage message) {
    }

    private static UpdateNpcDataMessage decode(ByteBuf buf) {
        FriendlyByteBuf friendlyBuf = new FriendlyByteBuf(buf);
        UUID uuid = friendlyBuf.readUUID();
        ResourceLocation modelId = ResourceLocation.parse(friendlyBuf.readUtf());
        ResourceLocation textureId = ResourceLocation.parse(friendlyBuf.readUtf());
        return new UpdateNpcDataMessage(uuid, modelId, textureId);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(UpdateNpcDataMessage message, IPayloadContext context) {
        context.enqueueWork(() -> handleMessage(message));
    }

    private static void handleMessage(UpdateNpcDataMessage message) {
        try {
            Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
            java.lang.reflect.Method getInstanceMethod = minecraftClass.getMethod("getInstance");
            Object mc = getInstanceMethod.invoke(null);
            
            java.lang.reflect.Field playerField = minecraftClass.getDeclaredField("player");
            Object localPlayer = playerField.get(mc);
            
            if (localPlayer != null) {
                Class<?> npcDataClass = Class.forName("moe.byn.minecraftmod.legacyysm.bukkit.client.NPCData");
                java.lang.reflect.Method putMethod = npcDataClass.getMethod("put", UUID.class, ResourceLocation.class, ResourceLocation.class);
                putMethod.invoke(null, message.uuid, message.modelId, message.textureId);
            }
        } catch (Exception e) {
            YesSteveModel.LOGGER.error("Failed to handle UpdateNpcDataMessage", e);
        }
    }
}
