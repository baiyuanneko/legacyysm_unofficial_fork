package moe.byn.minecraftmod.legacyysm.client.compat;

import moe.byn.minecraftmod.legacyysm.client.model.CustomPlayerModel;
import moe.byn.minecraftmod.legacyysm.geckolib3.core.processor.IBone;

/**
 * Compatibility stub for FirstPersonMod.
 * The actual implementation requires the firstpersonmod to be installed.
 * This class provides safe defaults when the mod is not present.
 */
public class FirstPersonCompat {
    
    public static void hideHead(IBone head) {
        // No-op - requires firstpersonmod API
    }

    public static void registerOffset() {
        // No-op - requires firstpersonmod API
    }

    public static boolean isHeadHide() {
        return false;
    }
}
