package moe.byn.minecraftmod.legacyysm.client.gui.button;

import moe.byn.minecraftmod.legacyysm.capability.YSMAttachments;
import moe.byn.minecraftmod.legacyysm.client.ClientModelManager;
import moe.byn.minecraftmod.legacyysm.util.Keep;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class TextureCountButton extends FlatColorButton {
    public TextureCountButton(int x, int y) {
        super(x, y, 20, 20, Component.empty(), (b) -> {
        });
    }

    @Override
    @Keep
    public Component getMessage() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            var cap = player.getData(YSMAttachments.MODEL_INFO);
            ResourceLocation modelId = cap.getModelId();
            if (ClientModelManager.MODELS.containsKey(modelId)) {
                String countText = String.valueOf(ClientModelManager.MODELS.get(modelId).size());
                return Component.literal(countText);
            }
        }
        return super.getMessage();
    }
}
