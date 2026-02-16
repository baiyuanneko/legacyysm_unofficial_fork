package moe.byn.minecraftmod.legacyysm.client.event;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import moe.byn.minecraftmod.legacyysm.client.entity.CustomPlayerEntity;
import moe.byn.minecraftmod.legacyysm.event.api.SpecialPlayerRenderEvent;
import moe.byn.minecraftmod.legacyysm.util.ModelIdUtil;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.Map;

@EventBusSubscriber(value = Dist.CLIENT, modid = YesSteveModel.MOD_ID)
public class VanillaPlayerRenderEvent {
    private static final ResourceLocation STEVE_SKIN_LOCATION = ResourceLocation.parse("textures/entity/player/wide/steve.png");
    private static final ResourceLocation ALEX_SKIN_LOCATION = ResourceLocation.parse("textures/entity/player/slim/alex.png");
    private static final String STEVE = "steve";
    private static final String ALEX = "alex";

    @SubscribeEvent
    public static void onRenderPlayer(SpecialPlayerRenderEvent event) {
        Player player = event.getPlayer();
        CustomPlayerEntity animatable = event.getCustomPlayer();
        if (isVanillaPlayer(event.getModelId()) && player instanceof AbstractClientPlayer clientPlayer) {
            animatable.setPlayer(player);
            animatable.setMainModel(ModelIdUtil.getMainId(event.getModelId()));
            ResourceLocation location;
            Minecraft minecraft = Minecraft.getInstance();
            location = minecraft.getSkinManager().getInsecureSkin(clientPlayer.getGameProfile()).texture();
            if (location == null) {
                location = getDefaultSkin(event.getModelId());
            }
            animatable.setTexture(location);
        }
    }

    private static boolean isVanillaPlayer(ResourceLocation modelId) {
        return modelId.getPath().equals(STEVE) || modelId.getPath().equals(ALEX);
    }

    private static ResourceLocation getDefaultSkin(ResourceLocation modelId) {
        return modelId.getPath().equals(STEVE) ? STEVE_SKIN_LOCATION : ALEX_SKIN_LOCATION;
    }
}
