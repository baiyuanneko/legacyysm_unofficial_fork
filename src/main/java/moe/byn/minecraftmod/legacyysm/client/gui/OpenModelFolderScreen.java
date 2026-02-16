package moe.byn.minecraftmod.legacyysm.client.gui;

import moe.byn.minecraftmod.legacyysm.model.ServerModelManager;
import moe.byn.minecraftmod.legacyysm.util.Keep;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class OpenModelFolderScreen extends Screen {
    private final PlayerModelScreen screen;

    protected OpenModelFolderScreen(PlayerModelScreen screen) {
        super(Component.literal("Open Model Folder"));
        this.screen = screen;
    }

    @Override
    @Keep
    protected void init() {
        int x = (width - 310) / 2;
        int y = height / 2 + 60;
        this.clearWidgets();
        this.addRenderableWidget(Button.builder(Component.translatable("gui.legacyysm_byn.open_model_folder.open"), b -> {
            Util.getPlatform().openFile(ServerModelManager.CUSTOM.toFile());
        }).bounds(x, y, 150, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.legacyysm_byn.model.return"), b -> {
            getMinecraft().setScreen(this.screen);
        }).bounds(x + 160, y, 150, 20).build());
    }

    @Override
    @Keep
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fillGradient(0, 0, this.width, this.height, 0xc0101010, 0xc0101010);
    }

    @Override
    @Keep
    public void render(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(graphics, pMouseX, pMouseY, pPartialTick);
        graphics.drawWordWrap(font, Component.translatable("gui.legacyysm_byn.open_model_folder.tips"),
                (width - 400) / 2, height / 2 - 80, 400, 0XFFFFFF);
    }
}
