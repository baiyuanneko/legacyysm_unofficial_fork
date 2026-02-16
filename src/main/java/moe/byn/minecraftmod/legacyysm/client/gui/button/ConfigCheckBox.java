package moe.byn.minecraftmod.legacyysm.client.gui.button;

import moe.byn.minecraftmod.legacyysm.util.Keep;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ConfigCheckBox extends AbstractWidget {
    private final Checkbox checkbox;
    private final ModConfigSpec.BooleanValue configSpec;

    public ConfigCheckBox(int pX, int pY, String key, ModConfigSpec.BooleanValue configSpec) {
        super(pX, pY, 400, 20, Component.translatable("gui.legacyysm_byn.config." + key));
        this.configSpec = configSpec;
        this.checkbox = Checkbox.builder(Component.translatable("gui.legacyysm_byn.config." + key), Minecraft.getInstance().font)
                .pos(pX, pY)
                .selected(configSpec.get())
                .onValueChange((checkbox, selected) -> {
                    configSpec.set(selected);
                })
                .build();
    }

    @Override
    @Keep
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.checkbox.renderWidget(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    @Keep
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.checkbox.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    @Keep
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        this.checkbox.updateWidgetNarration(narration);
    }
}
