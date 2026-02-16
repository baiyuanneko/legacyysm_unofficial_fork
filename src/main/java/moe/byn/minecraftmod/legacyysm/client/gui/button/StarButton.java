package moe.byn.minecraftmod.legacyysm.client.gui.button;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import moe.byn.minecraftmod.legacyysm.capability.YSMAttachments;
import moe.byn.minecraftmod.legacyysm.network.NetworkHandler;
import moe.byn.minecraftmod.legacyysm.network.message.SetStarModel;
import moe.byn.minecraftmod.legacyysm.util.Keep;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class StarButton extends FlatColorButton {
    private final static ResourceLocation ICON = ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "texture/icon.png");

    public StarButton(int x, int y) {
        super(x, y, 20, 20, Component.empty(), (b) -> {
        });
    }

    @Override
    @Keep
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float pPartialTick) {
        super.renderWidget(graphics, mouseX, mouseY, pPartialTick);
        int startX = (this.width - 16) / 2;
        int startY = (this.height - 16) / 2;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            var modelInfoCap = player.getData(YSMAttachments.MODEL_INFO);
            var starModelsCap = player.getData(YSMAttachments.STAR_MODELS);
            ResourceLocation modelId = modelInfoCap.getModelId();
            if (starModelsCap.containModel(modelId)) {
                graphics.blit(ICON, this.getX() + startX, this.getY() + startY, 16, 16, 16, 0, 16, 16, 256, 256);
            } else {
                graphics.blit(ICON, this.getX() + startX, this.getY() + startY, 16, 16, 0, 0, 16, 16, 256, 256);
            }
        }
    }

    @Override
    @Keep
    public void onPress() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            var modelInfoCap = player.getData(YSMAttachments.MODEL_INFO);
            var starModelsCap = player.getData(YSMAttachments.STAR_MODELS);
            ResourceLocation modelId = modelInfoCap.getModelId();
            if (starModelsCap.containModel(modelId)) {
                starModelsCap.removeModel(modelId);
                NetworkHandler.sendToServer(SetStarModel.remove(modelId));
            } else {
                starModelsCap.addModel(modelId);
                NetworkHandler.sendToServer(SetStarModel.add(modelId));
            }
        }
    }
}
