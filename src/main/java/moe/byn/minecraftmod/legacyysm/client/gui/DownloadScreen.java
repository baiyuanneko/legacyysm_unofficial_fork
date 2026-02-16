package moe.byn.minecraftmod.legacyysm.client.gui;

import moe.byn.minecraftmod.legacyysm.client.gui.button.FlatColorButton;
import moe.byn.minecraftmod.legacyysm.util.Keep;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class DownloadScreen extends Screen {
    private final PlayerModelScreen parent;
    private int x;
    private int y;

    public DownloadScreen(PlayerModelScreen parent) {
        super(Component.literal("YSM Config GUI"));
        this.parent = parent;
    }

    @Override
    @Keep
    protected void init() {
        this.x = (width - 420) / 2;
        this.y = (height - 235) / 2;

        addRenderableWidget(new FlatColorButton(x + 5, y, 80, 18, Component.translatable("gui.legacyysm_byn.model.return"), (b) -> this.getMinecraft().setScreen(parent)));
    }

    @Override
    @Keep
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fillGradient(0, 0, this.width, this.height, 0xc0101010, 0xc0101010);
    }

    @Override
    @Keep
    public void render(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        graphics.drawCenteredString(font, "Coming Soooooooooooooooooooooooooon™", width / 2, height / 2 - 5, ChatFormatting.DARK_RED.getColor());
        super.render(graphics, pMouseX, pMouseY, pPartialTick);
    }
}