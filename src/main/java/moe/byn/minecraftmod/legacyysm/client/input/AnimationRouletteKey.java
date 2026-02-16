package moe.byn.minecraftmod.legacyysm.client.input;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import moe.byn.minecraftmod.legacyysm.client.gui.AnimationRouletteScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(value = Dist.CLIENT, modid = YesSteveModel.MOD_ID)
public class AnimationRouletteKey {
    public static final KeyMapping ANIMATION_ROULETTE_KEY = new KeyMapping("key.legacyysm_byn.animation_roulette.desc",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,
            "key.category.legacyysm_byn");

    @SubscribeEvent
    public static void onKeyboardInput(InputEvent.Key event) {
        if (ANIMATION_ROULETTE_KEY.isDown()) {
            Minecraft.getInstance().setScreen(new AnimationRouletteScreen());
        }
    }
}
