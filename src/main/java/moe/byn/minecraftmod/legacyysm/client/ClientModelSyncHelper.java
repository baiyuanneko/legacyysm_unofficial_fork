package moe.byn.minecraftmod.legacyysm.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ClientModelSyncHelper {
    
    private ClientModelSyncHelper() {}
    
    public static void sendSyncModelMessage() {
        ClientModelManager.sendSyncModelMessage();
    }
}
