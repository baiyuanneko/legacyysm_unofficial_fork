package moe.byn.minecraftmod.legacyysm.network.message;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import moe.byn.minecraftmod.legacyysm.capability.YSMAttachments;
import com.google.common.collect.Sets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Set;

public class SyncAuthModels implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncAuthModels> TYPE = 
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "sync_auth_models"));
    
    public static final StreamCodec<ByteBuf, SyncAuthModels> STREAM_CODEC = StreamCodec.of(
            SyncAuthModels::encode,
            SyncAuthModels::decode
    );
    
    private static void encode(ByteBuf buf, SyncAuthModels msg) {
        FriendlyByteBuf friendlyBuf = new FriendlyByteBuf(buf);
        friendlyBuf.writeVarInt(msg.authModels.size());
        for (ResourceLocation modelId : msg.authModels) {
            friendlyBuf.writeResourceLocation(modelId);
        }
    }
    
    private static SyncAuthModels decode(ByteBuf buf) {
        FriendlyByteBuf friendlyBuf = new FriendlyByteBuf(buf);
        int size = friendlyBuf.readVarInt();
        Set<ResourceLocation> tmp = Sets.newHashSet();
        for (int i = 0; i < size; i++) {
            tmp.add(friendlyBuf.readResourceLocation());
        }
        return new SyncAuthModels(tmp);
    }

    private final Set<ResourceLocation> authModels;

    public SyncAuthModels(Set<ResourceLocation> authModels) {
        this.authModels = authModels;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(SyncAuthModels message, IPayloadContext context) {
        context.enqueueWork(() -> handleCapability(message));
    }

    private static void handleCapability(SyncAuthModels message) {
        try {
            Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
            java.lang.reflect.Method getInstanceMethod = minecraftClass.getMethod("getInstance");
            Object mc = getInstanceMethod.invoke(null);
            
            java.lang.reflect.Field playerField = minecraftClass.getDeclaredField("player");
            Object player = playerField.get(mc);
            
            if (player != null) {
                Class<?> playerClass = player.getClass();
                java.lang.reflect.Method getDataMethod = playerClass.getMethod("getData", 
                    Class.forName("net.neoforged.neoforge.attachment.AttachmentType"));
                
                Class<?> attachmentsClass = Class.forName("moe.byn.minecraftmod.legacyysm.capability.YSMAttachments");
                java.lang.reflect.Field authModelsField = attachmentsClass.getField("AUTH_MODELS");
                Object authModelsAttachment = authModelsField.get(null);
                
                Object authModelsCap = getDataMethod.invoke(player, authModelsAttachment);
                
                Class<?> capClass = authModelsCap.getClass();
                java.lang.reflect.Method setAuthModelsMethod = capClass.getMethod("setAuthModels", Set.class);
                setAuthModelsMethod.invoke(authModelsCap, message.authModels);
            }
        } catch (Exception e) {
            YesSteveModel.LOGGER.error("Failed to handle SyncAuthModels", e);
        }
    }
}
