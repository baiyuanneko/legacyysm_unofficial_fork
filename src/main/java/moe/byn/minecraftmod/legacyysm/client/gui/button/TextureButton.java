package moe.byn.minecraftmod.legacyysm.client.gui.button;

import moe.byn.minecraftmod.legacyysm.capability.YSMAttachments;
import moe.byn.minecraftmod.legacyysm.network.NetworkHandler;
import moe.byn.minecraftmod.legacyysm.bukkit.message.OpenModelGuiMessage;
import moe.byn.minecraftmod.legacyysm.network.message.SetModelAndTexture;
import moe.byn.minecraftmod.legacyysm.bukkit.message.SetNpcModelAndTexture;
import moe.byn.minecraftmod.legacyysm.util.Keep;
import moe.byn.minecraftmod.legacyysm.util.ModelIdUtil;
import moe.byn.minecraftmod.legacyysm.util.RenderUtil;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class TextureButton extends Button {
    private final ResourceLocation modelId;
    private final ResourceLocation textureId;
    private final String name;
    private final Player player;

    public TextureButton(int pX, int pY, ResourceLocation modelId, ResourceLocation textureId, Player player) {
        super(pX, pY, 54, 102, Component.empty(), (b) -> {
        }, DEFAULT_NARRATION);
        this.modelId = modelId;
        this.textureId = textureId;
        this.name = ModelIdUtil.getSubNameFromId(textureId);
        this.player = player;
    }

    @Override
    @Keep
    public void onPress() {
        var cap = player.getData(YSMAttachments.MODEL_INFO);
        cap.setModelAndTexture(modelId, textureId);
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (player.equals(localPlayer)) {
            NetworkHandler.sendToServer(new SetModelAndTexture(modelId, textureId));
        } else {
            NetworkHandler.sendToServer(new SetNpcModelAndTexture(modelId, textureId, OpenModelGuiMessage.CURRENT_NPC_ID));
        }
    }

    @Override
    @Keep
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;

        graphics.fillGradient(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xFF_434242, 0xFF_434242);
        Window window = Minecraft.getInstance().getWindow();
        double scale = window.getGuiScale();
        int scissorX = (int) (this.getX() * scale);
        int scissorY = (int) (window.getHeight() - ((this.getY() + this.height - 20) * scale));
        int scissorW = (int) (this.width * scale);
        int scissorH = (int) ((this.height - 20) * scale);
        RenderSystem.enableScissor(scissorX, scissorY, scissorW, scissorH);
        RenderUtil.renderEntityInInventory(this.getX() + this.width / 2, this.getY() + this.height / 2 + 24, 35, minecraft.player, modelId, textureId);
        RenderSystem.disableScissor();

        Component message = Component.literal(name);
        List<FormattedCharSequence> split = font.split(message, 50);
        if (split.size() > 1) {
            graphics.drawCenteredString(font, split.get(0), this.getX() + this.width / 2, this.getY() + this.height - 19, 0xF3EFE0);
            graphics.drawCenteredString(font, split.get(1), this.getX() + this.width / 2, this.getY() + this.height - 10, 0xF3EFE0);
        } else {
            graphics.drawCenteredString(font, message, this.getX() + this.width / 2, this.getY() + this.height - 15, 0xF3EFE0);
        }
        if (this.isHoveredOrFocused()) {
            graphics.fillGradient(this.getX(), this.getY() + 1, this.getX() + 1, this.getY() + this.height - 1, 0xff_F3EFE0, 0xff_F3EFE0);
            graphics.fillGradient(this.getX(), this.getY(), this.getX() + this.width, this.getY() + 1, 0xff_F3EFE0, 0xff_F3EFE0);
            graphics.fillGradient(this.getX() + this.width - 1, this.getY() + 1, this.getX() + this.width, this.getY() + this.height - 1, 0xff_F3EFE0, 0xff_F3EFE0);
            graphics.fillGradient(this.getX(), this.getY() + this.height - 1, this.getX() + this.width, this.getY() + this.height, 0xff_F3EFE0, 0xff_F3EFE0);
        }
    }
}
