package moe.byn.minecraftmod.legacyysm.client.input;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(value = Dist.CLIENT, modid = YesSteveModel.MOD_ID)
public class DebugAnimationKey {
    public static boolean DEBUG = false;

    public static final KeyMapping DEBUG_ANIMATION_KEY = new KeyMapping("key.legacyysm_byn.debug_animation.desc",
            KeyConflictContext.IN_GAME, KeyModifier.ALT,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B,
            "key.category.legacyysm_byn");

    @SubscribeEvent
    public static void onKeyboardInput(InputEvent.Key event) {
        if (DEBUG_ANIMATION_KEY.isDown()) {
            DEBUG = !DEBUG;
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) {
                return;
            }
            if (DEBUG) {
                Minecraft.getInstance().player.sendSystemMessage(Component.translatable("message.legacyysm_byn.model.debug_animation.true"));
            } else {
                Minecraft.getInstance().player.sendSystemMessage(Component.translatable("message.legacyysm_byn.model.debug_animation.false"));
            }
        }
    }
}
