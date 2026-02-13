package com.elfmcys.yesstevemodel.bukkit.event;

import com.elfmcys.yesstevemodel.bukkit.client.NPCData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.event.entity.player.PlayerEvent;
import net.neoforged.eventbus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClearNPCdataEvent {
    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        NPCData.clear();
    }
}