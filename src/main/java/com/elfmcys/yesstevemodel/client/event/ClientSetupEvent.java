package com.elfmcys.yesstevemodel.client.event;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.animation.AnimationRegister;
import com.elfmcys.yesstevemodel.client.compat.FirstPersonCompat;
import com.elfmcys.yesstevemodel.client.gui.DebugAnimationScreen;
import com.elfmcys.yesstevemodel.client.gui.ExtraPlayerScreen;
import com.elfmcys.yesstevemodel.client.input.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.client.event.RegisterGuiOverlaysEvent;
import net.neoforged.client.event.RegisterKeyMappingsEvent;
import net.neoforged.eventbus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import static net.neoforged.client.gui.overlay.VanillaGuiOverlay.DEBUG_TEXT;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT, modid = YesSteveModel.MOD_ID)
public class ClientSetupEvent {
    public static final String FIRST_PERSON_MOD_ID = "firstpersonmod";

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        AnimationRegister.registerAnimationState();
        AnimationRegister.registerVariables();
        if (ModList.get().isLoaded(FIRST_PERSON_MOD_ID)) {
            FirstPersonCompat.registerOffset();
        }
    }

    @SubscribeEvent
    public static void onClientSetup(RegisterKeyMappingsEvent event) {
        event.register(PlayerModelScreenKey.PLAYER_MODEL_KEY);
        event.register(AnimationRouletteKey.ANIMATION_ROULETTE_KEY);
        event.register(DebugAnimationKey.DEBUG_ANIMATION_KEY);
        event.register(ExtraPlayerConfigKey.EXTRA_PLAYER_RENDER_KEY);
        ExtraAnimationKey.registerKeyBinding(event);
    }

    @SubscribeEvent
    public static void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(DEBUG_TEXT.id(), "ysm_debug_info", new DebugAnimationScreen());
        event.registerAbove(DEBUG_TEXT.id(), "ysm_extra_player", new ExtraPlayerScreen());
    }
}
