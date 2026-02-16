package moe.byn.minecraftmod.legacyysm.client.event;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import moe.byn.minecraftmod.legacyysm.client.animation.AnimationRegister;
import moe.byn.minecraftmod.legacyysm.client.compat.FirstPersonCompat;
import moe.byn.minecraftmod.legacyysm.client.gui.DebugAnimationScreen;
import moe.byn.minecraftmod.legacyysm.client.gui.ExtraPlayerScreen;
import moe.byn.minecraftmod.legacyysm.client.input.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT, modid = YesSteveModel.MOD_ID)
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
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerBelowAll(ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "ysm_debug_info"), new DebugAnimationScreen());
        event.registerBelowAll(ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "ysm_extra_player"), new ExtraPlayerScreen());
    }
}
