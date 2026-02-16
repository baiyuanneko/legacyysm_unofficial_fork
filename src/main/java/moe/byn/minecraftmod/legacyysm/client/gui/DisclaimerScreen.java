package moe.byn.minecraftmod.legacyysm.client.gui;

import moe.byn.minecraftmod.legacyysm.config.GeneralConfig;
import moe.byn.minecraftmod.legacyysm.util.Keep;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class DisclaimerScreen extends Screen {
    private Checkbox readCheckbox;
    private int x;
    private int y;

    public DisclaimerScreen() {
        super(Component.literal("Disclaimer GUI"));
    }

    @Override
    @Keep
    protected void init() {
        this.clearWidgets();

        MutableComponent mainText = Component.translatable("gui.legacyysm_byn.disclaimer.text");
        List<FormattedCharSequence> splitMainText = font.split(mainText, 400);
        int totalHeight = splitMainText.size() * font.lineHeight + 20 + 20 + 10 + 20;
        this.x = (width - 400) / 2;
        this.y = (height - totalHeight) / 2;

        MutableComponent readCheckboxText = Component.translatable("gui.legacyysm_byn.disclaimer.read");
        int readTextWidth = font.width(readCheckboxText);
        readCheckbox = Checkbox.builder(readCheckboxText, font)
                .pos((width - readTextWidth) / 2, y + totalHeight - 50)
                .selected(!GeneralConfig.DISCLAIMER_SHOW.get())
                .build();
        addRenderableWidget(readCheckbox);
        addRenderableWidget(new Button.Builder(Component.translatable("gui.legacyysm_byn.disclaimer.close"), b -> {
            if (readCheckbox.selected()) {
                GeneralConfig.DISCLAIMER_SHOW.set(false);
                GeneralConfig.CONFIG_SPEC.save();
                Minecraft.getInstance().setScreen(new PlayerModelScreen());
            } else {
                Minecraft.getInstance().setScreen(null);
            }
        }).size(300, 20).pos((width - 300) / 2, y + totalHeight - 20).build());
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
        graphics.drawWordWrap(font, Component.translatable("gui.legacyysm_byn.disclaimer.text"), x, y, 400, 0xffffffff);
    }
}
