package moe.byn.minecraftmod.legacyysm.client.gui;

import moe.byn.minecraftmod.legacyysm.capability.YSMAttachments;
import moe.byn.minecraftmod.legacyysm.client.ClientModelManager;
import moe.byn.minecraftmod.legacyysm.client.input.ExtraAnimationKey;
import moe.byn.minecraftmod.legacyysm.config.GeneralConfig;
import moe.byn.minecraftmod.legacyysm.network.NetworkHandler;
import moe.byn.minecraftmod.legacyysm.network.message.SetPlayAnimation;
import moe.byn.minecraftmod.legacyysm.util.Keep;
import moe.byn.minecraftmod.legacyysm.util.ModelIdUtil;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.StringUtils;
import org.joml.Matrix4f;

public class AnimationRouletteScreen extends Screen {
    private int x;
    private int y;
    private int selectId = -1;
    private String[] names;

    public AnimationRouletteScreen() {
        super(Component.literal("Animation Roulette GUI"));
    }

    @Override
    @Keep
    protected void init() {
        this.x = width / 2;
        this.y = height / 2 - 8;

        if (minecraft != null && minecraft.player != null) {
            var cap = minecraft.player.getData(YSMAttachments.MODEL_INFO);
            ResourceLocation modelId = cap.getModelId();
            if (ClientModelManager.EXTRA_ANIMATION_NAME.containsKey(ModelIdUtil.getMainId(modelId))) {
                this.names = ClientModelManager.EXTRA_ANIMATION_NAME.get(ModelIdUtil.getMainId(modelId));
            }
        }
    }

    @Override
    @Keep
    public void render(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        drawRoulette(graphics.pose(), pMouseX, pMouseY);
        drawRouletteText(graphics);
    }

    @Override
    @Keep
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (-1 < selectId && selectId < 8 && minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            NetworkHandler.sendToServer(new SetPlayAnimation(selectId));
            if (minecraft.player != null && GeneralConfig.PRINT_ANIMATION_ROULETTE_MSG.get()) {
                minecraft.player.sendSystemMessage(Component.translatable("message.legacyysm_byn.model.animation_roulette.play", selectId));
            }
            minecraft.setScreen(null);
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    @Keep
    public boolean isPauseScreen() {
        return false;
    }

    private void drawRouletteText(GuiGraphics graphics) {
        int count = 8;
        float startDeg = Mth.PI / count;
        for (int i = 0; i < count; i++) {
            int r = 65;
            MutableComponent keyText = Component.literal("[ ").withStyle(ChatFormatting.YELLOW);
            KeyMapping keyMapping = ExtraAnimationKey.EXTRA_ANIMATION_KEYS.get(i);
            if (keyMapping.getKey() == InputConstants.UNKNOWN) {
                keyText.append(Component.translatable("key.legacyysm_byn.extra_animation.none"));
            } else {
                keyText.append(keyMapping.getTranslatedKeyMessage());
            }
            keyText.append(" ]");
            if (this.names != null && this.names.length > i && StringUtils.isNoneBlank(this.names[i])) {
                graphics.drawCenteredString(font, Component.literal(this.names[i]), (int) (x + r * Mth.cos(startDeg)), (int) (y + r * Mth.sin(startDeg) - font.lineHeight / 2 - 8), 0xF3EFE0);
            } else {
                graphics.drawCenteredString(font, String.valueOf(i), (int) (x + r * Mth.cos(startDeg)), (int) (y + r * Mth.sin(startDeg) - font.lineHeight / 2 - 8), 0xF3EFE0);
            }
            graphics.drawCenteredString(font, keyText, (int) (x + r * Mth.cos(startDeg)), (int) (y + r * Mth.sin(startDeg) - font.lineHeight / 2 + 4), 0xF3EFE0);
            startDeg = startDeg + 2 * Mth.PI / count;
        }
    }

    private void drawRoulette(PoseStack pPoseStack, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f pMatrix = pPoseStack.last().pose();

        int count = 8;
        float theta = (float) Mth.atan2(mouseY - y, mouseX - x);
        if (theta < 0) {
            theta = Mth.PI * 2 + theta;
        }
        float distance = Mth.sqrt(Mth.square(mouseY - y) + Mth.square(mouseX - x));
        boolean isSelected = false;
        for (int i = 0; i < count; i++) {
            float spacingDeg = Mth.PI / 90;
            float startDeg = (2 * Mth.PI / count) * i + spacingDeg;
            float endDeg = (2 * Mth.PI / count) * (i + 1) - spacingDeg;
            if (startDeg < theta && theta < endDeg && 50 < distance && distance < 100) {
                drawFan(bufferbuilder, pMatrix, 25, 105, startDeg, endDeg, 0xf0FFB100);
                isSelected = true;
                this.selectId = i;
            } else {
                drawFan(bufferbuilder, pMatrix, 25, 105, startDeg, endDeg, 0x90000000);
            }
        }
        if (!isSelected) {
            this.selectId = -1;
        }

        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
        RenderSystem.disableBlend();
    }

    private void drawFan(BufferBuilder builder, Matrix4f matrix4f, float rIn, float rOut, float startDeg, float endDeg, int color) {
        float alpha = (color >> 24 & 255) / 255.0F;
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        builder.addVertex(matrix4f, x + rOut * Mth.cos(startDeg), y + rOut * Mth.sin(startDeg), 0).setColor(red, green, blue, alpha);
        builder.addVertex(matrix4f, x + rIn * Mth.cos(startDeg), y + rIn * Mth.sin(startDeg), 0).setColor(red, green, blue, alpha);
        builder.addVertex(matrix4f, x + rIn * Mth.cos(endDeg), y + rIn * Mth.sin(endDeg), 0).setColor(red, green, blue, alpha);
        builder.addVertex(matrix4f, x + rOut * Mth.cos(endDeg), y + rOut * Mth.sin(endDeg), 0).setColor(red, green, blue, alpha);
    }
}
