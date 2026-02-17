package moe.byn.minecraftmod.legacyysm.event;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import moe.byn.minecraftmod.legacyysm.model.ServerModelManager;
import moe.byn.minecraftmod.legacyysm.model.PlayerModelCache;
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
        }
    }
    
    @SubscribeEvent
    public static void onLoggedOutServer(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer) {
            PlayerModelCache.onPlayerDisconnect(event.getEntity().getUUID());
        }
    }
}
