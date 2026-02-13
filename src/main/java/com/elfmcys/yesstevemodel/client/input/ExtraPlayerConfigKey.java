package com.elfmcys.yesstevemodel.client.input;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.gui.ExtraPlayerConfigScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.client.event.InputEvent;
import net.neoforged.client.settings.KeyConflictContext;
import net.neoforged.client.settings.KeyModifier;
import net.neoforged.eventbus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = YesSteveModel.MOD_ID)
public class ExtraPlayerConfigKey {
    public static final KeyMapping EXTRA_PLAYER_RENDER_KEY = new KeyMapping("key.yes_steve_model.open_extra_player_render.desc",
            KeyConflictContext.IN_GAME,
            KeyModifier.ALT,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "key.category.yes_steve_model");

    @SubscribeEvent
    public static void onKeyboardInput(InputEvent.Key event) {
        if (EXTRA_PLAYER_RENDER_KEY.isDown()) {
            Minecraft.getInstance().setScreen(new ExtraPlayerConfigScreen());
        }
    }
}