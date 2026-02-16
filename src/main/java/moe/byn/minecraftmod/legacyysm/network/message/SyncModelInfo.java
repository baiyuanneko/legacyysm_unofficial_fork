package moe.byn.minecraftmod.legacyysm.network.message;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import moe.byn.minecraftmod.legacyysm.capability.ModelInfoCapability;
import moe.byn.minecraftmod.legacyysm.capability.YSMAttachments;
import moe.byn.minecraftmod.legacyysm.util.ThreadTools;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SyncModelInfo implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncModelInfo> TYPE = 
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "sync_model_info"));
    
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncModelInfo> STREAM_CODEC = StreamCodec.of(
            SyncModelInfo::encode,
            SyncModelInfo::decode
    );
    
    private static void encode(RegistryFriendlyByteBuf buf, SyncModelInfo msg) {
        buf.writeVarInt(msg.entityId);
        buf.writeNbt(msg.capability.serializeNBT(buf.registryAccess()));
    }
    
    private static SyncModelInfo decode(RegistryFriendlyByteBuf buf) {
        int entityId = buf.readVarInt();
        CompoundTag compoundTag = buf.readNbt();
        ModelInfoCapability cap = new ModelInfoCapability();
        if (compoundTag != null) {
            cap.deserializeNBT(buf.registryAccess(), compoundTag);
        }
        return new SyncModelInfo(entityId, cap);
    }

    private final int entityId;
    private final ModelInfoCapability capability;

    public SyncModelInfo(int entityId, ModelInfoCapability capability) {
        this.entityId = entityId;
        this.capability = capability;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(SyncModelInfo message, IPayloadContext context) {
        context.enqueueWork(() -> handleCapability(message));
    }

    private static void handleCapability(SyncModelInfo message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            ThreadTools.THREAD_POOL.submit(() -> {
                        try {
                            int time = 0;
                            while (mc.level.getEntity(message.entityId) == null && time < 5) {
                                Thread.sleep(500);
                                time++;
                            }
                            Entity entity = mc.level.getEntity(message.entityId);
                            if (entity instanceof Player player) {
                                player.getData(YSMAttachments.MODEL_INFO).copyFrom(message.capability);
                            }
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
        }
    }
}
