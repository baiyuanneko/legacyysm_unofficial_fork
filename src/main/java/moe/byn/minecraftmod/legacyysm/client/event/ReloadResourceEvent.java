package moe.byn.minecraftmod.legacyysm.client.event;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import moe.byn.minecraftmod.legacyysm.client.ClientModelManager;
import moe.byn.minecraftmod.legacyysm.network.message.RequestLoadModel;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD, modid = YesSteveModel.MOD_ID)
public class ReloadResourceEvent {
    public static final ResourceLocation BLOCK_ATLAS_TEXTURE = ResourceLocation.parse("textures/atlas/blocks.png");
    public static int DEBUG_BG_WIDTH = 1000;
    private static final Pattern INT_REG = Pattern.compile("^[0-9]*$");

    @SubscribeEvent
    public static void onTextureStitchEventPost(TextureAtlasStitchedEvent event) {
        if (BLOCK_ATLAS_TEXTURE.equals(event.getAtlas().location())) {
            ClientModelManager.loadDefaultModel();
            ClientModelManager.CACHE_MD5.forEach(RequestLoadModel::loadModel);
            Matcher matcher = INT_REG.matcher(I18n.get("molang.legacyysm_byn.bg_width"));
            if (matcher.matches()) {
                DEBUG_BG_WIDTH = Integer.parseInt(I18n.get("molang.legacyysm_byn.bg_width"));
            }
        }
    }
}
