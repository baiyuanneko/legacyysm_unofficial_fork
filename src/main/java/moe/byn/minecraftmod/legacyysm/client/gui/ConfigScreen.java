package moe.byn.minecraftmod.legacyysm.client.gui;

import moe.byn.minecraftmod.legacyysm.client.gui.button.ConfigCheckBox;
import moe.byn.minecraftmod.legacyysm.client.gui.button.FlatColorButton;
import moe.byn.minecraftmod.legacyysm.config.ExtraPlayerScreenConfig;
import moe.byn.minecraftmod.legacyysm.config.GeneralConfig;
import moe.byn.minecraftmod.legacyysm.util.Keep;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigScreen extends Screen {
    private final PlayerModelScreen parent;

    public ConfigScreen(PlayerModelScreen parent) {
        super(Component.literal("YSM Config GUI"));
        this.parent = parent;
    }

    @Override
    @Keep
    protected void init() {
        int x = (width - 420) / 2;
        int y = (height - 235) / 2;

        addRenderableWidget(new FlatColorButton(x + 5, y, 80, 18, Component.translatable("gui.legacyysm_byn.model.return"), (b) -> this.getMinecraft().setScreen(parent)));

        addRenderableWidget(new ConfigCheckBox(x + 5, y + 25, "disable_self_model", GeneralConfig.DISABLE_SELF_MODEL));
        addRenderableWidget(new ConfigCheckBox(x + 5, y + 47, "disable_other_model", GeneralConfig.DISABLE_OTHER_MODEL));
        addRenderableWidget(new ConfigCheckBox(x + 5, y + 69, "print_animation_roulette_msg", GeneralConfig.PRINT_ANIMATION_ROULETTE_MSG));
        addRenderableWidget(new ConfigCheckBox(x + 5, y + 91, "disable_self_hands", GeneralConfig.DISABLE_SELF_HANDS));
        addRenderableWidget(new ConfigCheckBox(x + 5, y + 112, "disable_player_render", ExtraPlayerScreenConfig.DISABLE_PLAYER_RENDER));
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
    }
}
