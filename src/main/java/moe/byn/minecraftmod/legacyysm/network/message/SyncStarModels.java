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

public class SyncStarModels implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncStarModels> TYPE = 
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "sync_star_models"));
    
    public static final StreamCodec<ByteBuf, SyncStarModels> STREAM_CODEC = StreamCodec.of(
            SyncStarModels::encode,
            SyncStarModels::decode
    );
    
    private static void encode(ByteBuf buf, SyncStarModels msg) {
        FriendlyByteBuf friendlyBuf = new FriendlyByteBuf(buf);
        friendlyBuf.writeVarInt(msg.starModels.size());
        for (ResourceLocation modelId : msg.starModels) {
            friendlyBuf.writeResourceLocation(modelId);
        }
    }
    
    private static SyncStarModels decode(ByteBuf buf) {
        FriendlyByteBuf friendlyBuf = new FriendlyByteBuf(buf);
        int size = friendlyBuf.readVarInt();
        Set<ResourceLocation> tmp = Sets.newHashSet();
        for (int i = 0; i < size; i++) {
            tmp.add(friendlyBuf.readResourceLocation());
        }
        return new SyncStarModels(tmp);
    }

    private final Set<ResourceLocation> starModels;

    public SyncStarModels(Set<ResourceLocation> starModels) {
        this.starModels = starModels;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(SyncStarModels message, IPayloadContext context) {
        context.enqueueWork(() -> handleCapability(message));
    }

    private static void handleCapability(SyncStarModels message) {
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
                java.lang.reflect.Field starModelsField = attachmentsClass.getField("STAR_MODELS");
                Object starModelsAttachment = starModelsField.get(null);
                
                Object starModelsCap = getDataMethod.invoke(player, starModelsAttachment);
                
                Class<?> capClass = starModelsCap.getClass();
                java.lang.reflect.Method setStarModelsMethod = capClass.getMethod("setStarModels", Set.class);
                setStarModelsMethod.invoke(starModelsCap, message.starModels);
            }
        } catch (Exception e) {
            YesSteveModel.LOGGER.error("Failed to handle SyncStarModels", e);
        }
    }
}
