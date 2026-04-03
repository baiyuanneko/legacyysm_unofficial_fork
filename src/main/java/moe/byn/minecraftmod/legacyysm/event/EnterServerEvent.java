package moe.byn.minecraftmod.legacyysm.event;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import moe.byn.minecraftmod.legacyysm.capability.YSMAttachments;
import moe.byn.minecraftmod.legacyysm.config.ServerConfig;
import moe.byn.minecraftmod.legacyysm.model.PlayerModelCache;
import moe.byn.minecraftmod.legacyysm.model.ServerModelManager;
import moe.byn.minecraftmod.legacyysm.network.NetworkHandler;
import moe.byn.minecraftmod.legacyysm.network.message.RequestUploadModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = YesSteveModel.MOD_ID)
public final class EnterServerEvent {
    @SubscribeEvent
    public static void onLoggedInServer(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ServerModelManager.sendRequestSyncModelMessage(serverPlayer);
            
            PlayerModelCache.sendCachedModelsToPlayer(serverPlayer, 
                    serverPlayer.getServer().getPlayerList());

            requestModelUploadIfNeeded(serverPlayer);
        }
    }
    
    @SubscribeEvent
    public static void onLoggedOutServer(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer) {
            PlayerModelCache.onPlayerDisconnect(event.getEntity().getUUID());
        }
    }

    /**
     * If the player's current model is not available in server-side caches,
     * ask the client to re-upload it (model sync feature).
     */
    private static void requestModelUploadIfNeeded(ServerPlayer serverPlayer) {
        if (!ServerConfig.ALLOW_MODEL_SYNC.get()) {
            return;
        }

        ResourceLocation modelLoc = serverPlayer.getData(YSMAttachments.MODEL_INFO).getModelId();
        String modelId = modelLoc.getPath();

        boolean isBuiltIn = modelId.equals("default") || modelId.equals("default_boy")
                || modelId.equals("steve") || modelId.equals("alex") || modelId.equals("qingluka")
                || modelId.equals("wine_fox");
        if (isBuiltIn) {
            return;
        }

        boolean onServer = ServerModelManager.CACHE_NAME_INFO.containsKey(modelId);
        boolean inPlayerCache = PlayerModelCache.hasCachedModel(serverPlayer.getUUID(), modelId);

        if (!onServer && !inPlayerCache) {
            YesSteveModel.LOGGER.info("Player {} rejoining with uncached model {}, requesting upload",
                    serverPlayer.getName().getString(), modelId);
            NetworkHandler.sendToClientPlayer(new RequestUploadModel(modelId), serverPlayer);
        }
    }
}
