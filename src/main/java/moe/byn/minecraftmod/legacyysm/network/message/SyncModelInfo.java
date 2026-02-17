package moe.byn.minecraftmod.legacyysm.network.message;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import moe.byn.minecraftmod.legacyysm.capability.ModelInfoCapability;
import moe.byn.minecraftmod.legacyysm.capability.YSMAttachments;
import moe.byn.minecraftmod.legacyysm.util.ThreadTools;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
    
    private static final Map<UUID, ModelInfoCapability> PENDING_MODEL_INFOS = new ConcurrentHashMap<>();

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
        Object mc = getMinecraftInstance();
        if (mc == null) return;
        
        Object level = getLevelFromMinecraft(mc);
        if (level == null) return;
        
        ThreadTools.THREAD_POOL.submit(() -> {
            try {
                int time = 0;
                Entity entity = null;
                while (time < 5) {
                    entity = getEntityFromLevel(level, message.entityId);
                    if (entity != null) break;
                    Thread.sleep(500);
                    time++;
                }
                if (entity instanceof Player player) {
                    ResourceLocation modelId = message.capability.getModelId();
                    boolean modelAvailable = isModelAvailable(modelId);
                    
                    if (modelAvailable) {
                        player.getData(YSMAttachments.MODEL_INFO).copyFrom(message.capability);
                    } else if (isServerAllowModelSync()) {
                        PENDING_MODEL_INFOS.put(player.getUUID(), message.capability);
                        YesSteveModel.LOGGER.debug("Deferring model update for player {} - model {} not yet available", 
                                player.getName().getString(), modelId);
                    } else {
                        player.getData(YSMAttachments.MODEL_INFO).copyFrom(message.capability);
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    private static boolean isModelAvailable(ResourceLocation modelId) {
        try {
            Class<?> clientModelManagerClass = Class.forName("moe.byn.minecraftmod.legacyysm.client.ClientModelManager");
            java.lang.reflect.Field modelsField = clientModelManagerClass.getDeclaredField("MODELS");
            @SuppressWarnings("unchecked")
            Map<ResourceLocation, ?> models = (Map<ResourceLocation, ?>) modelsField.get(null);
            
            java.lang.reflect.Field localModelsField = clientModelManagerClass.getDeclaredField("LOCAL_MODELS");
            @SuppressWarnings("unchecked")
            Map<ResourceLocation, ?> localModels = (Map<ResourceLocation, ?>) localModelsField.get(null);
            
            return models.containsKey(modelId) || localModels.containsKey(modelId);
        } catch (Exception e) {
            return false;
        }
    }
    
    private static boolean isServerAllowModelSync() {
        try {
            Class<?> clientModelManagerClass = Class.forName("moe.byn.minecraftmod.legacyysm.client.ClientModelManager");
            java.lang.reflect.Field field = clientModelManagerClass.getDeclaredField("SERVER_ALLOWS_MODEL_SYNC");
            return field.getBoolean(null);
        } catch (Exception e) {
            return false;
        }
    }
    
    public static void applyPendingModelInfo(UUID playerUuid, ResourceLocation modelId) {
        ModelInfoCapability pending = PENDING_MODEL_INFOS.remove(playerUuid);
        if (pending != null && pending.getModelId().equals(modelId)) {
            try {
                Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
                java.lang.reflect.Method getInstanceMethod = minecraftClass.getMethod("getInstance");
                Object mc = getInstanceMethod.invoke(null);
                
                Object level = getLevelFromMinecraft(mc);
                if (level == null) return;
                
                java.lang.reflect.Method getPlayerByUUIDMethod = level.getClass().getMethod("getPlayerByUUID", UUID.class);
                Object entity = getPlayerByUUIDMethod.invoke(level, playerUuid);
                
                if (entity instanceof Player player) {
                    player.getData(YSMAttachments.MODEL_INFO).copyFrom(pending);
                    YesSteveModel.LOGGER.debug("Applied pending model info for player {}", player.getName().getString());
                }
            } catch (Exception e) {
                YesSteveModel.LOGGER.error("Failed to apply pending model info", e);
            }
        }
    }
    
    private static Object getMinecraftInstance() {
        try {
            Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
            java.lang.reflect.Method getInstanceMethod = minecraftClass.getMethod("getInstance");
            return getInstanceMethod.invoke(null);
        } catch (Exception e) {
            return null;
        }
    }
    
    private static Object getLevelFromMinecraft(Object minecraft) {
        try {
            Class<?> minecraftClass = minecraft.getClass();
            java.lang.reflect.Field levelField = minecraftClass.getDeclaredField("level");
            return levelField.get(minecraft);
        } catch (Exception e) {
            return null;
        }
    }
    
    private static Entity getEntityFromLevel(Object level, int entityId) {
        try {
            Class<?> levelClass = level.getClass();
            java.lang.reflect.Method getEntityMethod = levelClass.getMethod("getEntity", int.class);
            return (Entity) getEntityMethod.invoke(level, entityId);
        } catch (Exception e) {
            return null;
        }
    }
}
