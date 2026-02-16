package moe.byn.minecraftmod.legacyysm.event;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import moe.byn.minecraftmod.legacyysm.capability.*;
import moe.byn.minecraftmod.legacyysm.model.ServerModelManager;
import moe.byn.minecraftmod.legacyysm.network.NetworkHandler;
import moe.byn.minecraftmod.legacyysm.network.message.SyncAuthModels;
import moe.byn.minecraftmod.legacyysm.network.message.SyncModelInfo;
import moe.byn.minecraftmod.legacyysm.network.message.SyncStarModels;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = YesSteveModel.MOD_ID)
public final class CapabilityEvent {

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player newPlayer = event.getEntity();
        
        // Copy data using the new Attachment API
        newPlayer.getData(YSMAttachments.MODEL_INFO).copyFrom(original.getData(YSMAttachments.MODEL_INFO));
        newPlayer.getData(YSMAttachments.AUTH_MODELS).copyFrom(original.getData(YSMAttachments.AUTH_MODELS));
        newPlayer.getData(YSMAttachments.STAR_MODELS).copyFrom(original.getData(YSMAttachments.STAR_MODELS));
    }

    @SubscribeEvent
    public static void onTrackingPlayer(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof Player trackPlayer) {
            Player player = event.getEntity();
            ModelInfoCapability cap = trackPlayer.getData(YSMAttachments.MODEL_INFO);
            SyncModelInfo syncMsg = new SyncModelInfo(trackPlayer.getId(), cap);
            NetworkHandler.sendToClientPlayer(syncMsg, player);
        }
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Player player) {
            ModelInfoCapability modelInfoCap = player.getData(YSMAttachments.MODEL_INFO);
            
            if (player instanceof ServerPlayer serverPlayer) {
                AuthModelsCapability authModelsCap = player.getData(YSMAttachments.AUTH_MODELS);
                NetworkHandler.sendToClientPlayer(new SyncAuthModels(authModelsCap.getAuthModels()), serverPlayer);
                
                if (ServerModelManager.AUTH_MODELS.contains(modelInfoCap.getModelId().getPath()) && !authModelsCap.containModel(modelInfoCap.getModelId())) {
                    ResourceLocation defaultModelId = ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "default");
                    ResourceLocation defaultTextureId = ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "default/default.png");
                    modelInfoCap.setModelAndTexture(defaultModelId, defaultTextureId);
                }
                
                SyncModelInfo syncMsg = new SyncModelInfo(serverPlayer.getId(), modelInfoCap);
                NetworkHandler.sendToClientPlayer(syncMsg, serverPlayer);
                
                StarModelsCapability starModelCap = player.getData(YSMAttachments.STAR_MODELS);
                NetworkHandler.sendToClientPlayer(new SyncStarModels(starModelCap.getStarModels()), serverPlayer);
            } else {
                modelInfoCap.markDirty();
            }
        }
    }

    /**
     * 同步客户端服务端数据
     */
    @SubscribeEvent
    public static void playerTickEvent(ServerTickEvent.Pre event) {
        // Handle player tick via ServerTickEvent for server-side sync
        // Note: Player tick events need to be handled differently in NeoForge 1.21.1
        // The sync logic should now be done via the Attachment system's automatic sync
    }
}
