package moe.byn.minecraftmod.legacyysm.client.gui;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import moe.byn.minecraftmod.legacyysm.capability.YSMAttachments;
import moe.byn.minecraftmod.legacyysm.client.ClientModelManager;
import moe.byn.minecraftmod.legacyysm.client.gui.button.*;
import moe.byn.minecraftmod.legacyysm.util.Keep;
import moe.byn.minecraftmod.legacyysm.util.ModelIdUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class PlayerModelScreen extends Screen {
    protected final Player player;
    private Map<ResourceLocation, List<ResourceLocation>> models = Maps.newHashMap();
    private List<ResourceLocation> modelOrderList;
    private int maxPage;
    private EditBox textField;
    private Category category;
    private int page;
    private int x;
    private int y;

    public PlayerModelScreen() {
        super(Component.literal("YSM Player Model GUI"));
        this.category = Category.ALL;
        this.player = Minecraft.getInstance().player;
    }

    public PlayerModelScreen(Player player) {
        super(Component.literal("YSM Player Model GUI"));
        this.category = Category.ALL;
        this.player = player;
    }

    private void calculateModelList() {
        models = Maps.newHashMap();
        if (this.category == Category.ALL) {
            this.models.putAll(ClientModelManager.MODELS);
        }
        if (this.category == Category.AUTH) {
            var cap = this.player.getData(YSMAttachments.AUTH_MODELS);
            for (ResourceLocation modelId : ClientModelManager.MODELS.keySet()) {
                if (cap.containModel(modelId) || !ClientModelManager.AUTH_MODELS.contains(modelId.getPath())) {
                    this.models.put(modelId, ClientModelManager.MODELS.get(modelId));
                }
            }
        }
        if (this.category == Category.STAR) {
            var cap = this.player.getData(YSMAttachments.STAR_MODELS);
            for (ResourceLocation modelId : ClientModelManager.MODELS.keySet()) {
                if (cap.containModel(modelId)) {
                    this.models.put(modelId, ClientModelManager.MODELS.get(modelId));
                }
            }
        }

        if (textField != null) {
            String search = this.textField.getValue().toLowerCase(Locale.US);
            models.entrySet().removeIf(next -> !next.getKey().getPath().contains(search));
        }
        this.modelOrderList = Lists.newArrayList(models.keySet());
        this.modelOrderList.sort(ResourceLocation::compareTo);
        this.maxPage = (models.size() - 1) / 10;
    }

    @Override
    @Keep
    protected void init() {
        this.clearWidgets();
        this.calculateModelList();

        this.x = (width - 420) / 2;
        this.y = (height - 235) / 2;

        String perText = "";
        boolean focus = false;
        if (textField != null) {
            perText = textField.getValue();
            focus = textField.isFocused();
        }
        textField = new EditBox(getMinecraft().font, x + 144, y + 6, 140, 16, Component.literal("YSM Search Box"));
        textField.setValue(perText);
        textField.setTextColor(0xF3EFE0);
        textField.setFocused(focus);
        textField.moveCursorToEnd(false);
        this.addWidget(this.textField);

        addRenderableWidget(new TextureCountButton(x + 5, y + 5));
        addRenderableWidget(new FlatIconButton(x + 28, y + 5, 79, 20, 32, 16, (b) -> {
            var cap = player.getData(YSMAttachments.MODEL_INFO);
            List<ResourceLocation> textures = ClientModelManager.MODELS.get(cap.getModelId());
            if (textures != null) {
                Minecraft.getInstance().setScreen(new PlayerTextureScreen(this, cap.getModelId(), textures));
            }
        }).setTooltips("gui.legacyysm_byn.model.texture"));
        addRenderableWidget(new StarButton(x + 110, y + 5));

        addRenderableWidget(new FlatIconButton(x + 328, y + 5, 18, 18, 32, 0, (b) -> {
            if (this.category != Category.ALL) {
                this.category = Category.ALL;
                this.page = 0;
                this.init();
            }
        }).setTooltips("gui.legacyysm_byn.all_models"));
        addRenderableWidget(new FlatIconButton(x + 308, y + 5, 18, 18, 48, 0, (b) -> {
            if (this.category != Category.AUTH) {
                this.category = Category.AUTH;
                this.page = 0;
                this.init();
            }
        }).setTooltips("gui.legacyysm_byn.auth_models"));
        addRenderableWidget(new FlatIconButton(x + 288, y + 5, 18, 18, 0, 0, (b) -> {
            if (this.category != Category.STAR) {
                this.category = Category.STAR;
                this.page = 0;
                this.init();
            }
        }).setTooltips("gui.legacyysm_byn.star_models"));

        addRenderableWidget(new FlatIconButton(x + 397, y + 5, 18, 18, 16, 16, (b) -> {
            this.getMinecraft().setScreen(new ConfigScreen(this));
        }).setTooltips("gui.legacyysm_byn.config"));
        addRenderableWidget(new FlatIconButton(x + 377, y + 5, 18, 18, 0, 16, (b) -> {
            this.getMinecraft().setScreen(new DownloadScreen(this));
        }).setTooltips("gui.legacyysm_byn.download"));
        addRenderableWidget(new FlatIconButton(x + 357, y + 5, 18, 18, 80, 0, (b) -> {
            this.getMinecraft().setScreen(new OpenModelFolderScreen(this));
        }).setTooltips("gui.legacyysm_byn.open_model_folder.open"));

        addRenderableWidget(new FlatColorButton(x + 198, y + 215, 52, 14, Component.translatable("gui.legacyysm_byn.pre_page"), (b) -> {
            if (this.page > 0) {
                this.page--;
                this.init();
            }
        }));
        addRenderableWidget(new FlatColorButton(x + 308, y + 215, 52, 14, Component.translatable("gui.legacyysm_byn.next_page"), (b) -> {
            if (this.page < this.maxPage) {
                this.page++;
                this.init();
            }
        }));

        if (this.page > this.maxPage) {
            this.page = 0;
        }

        for (int i = 0; i < 10; i++) {
            int modelIndex = i + this.page * 10;
            if (modelIndex >= models.size()) {
                break;
            }
            ResourceLocation id = modelOrderList.get(modelIndex);
            int xStart = x + 143 + 55 * (i % 5);
            int yStart = y + 28 + 93 * (i / 5);
            var authCap = player.getData(YSMAttachments.AUTH_MODELS);
            if (ClientModelManager.AUTH_MODELS.contains(id.getPath()) && !authCap.containModel(id)) {
                addRenderableWidget(new ModelButton(xStart, yStart, true, Pair.of(id, models.get(id)), ClientModelManager.EXTRA_INFO.get(ModelIdUtil.getMainId(id)), player));
            } else {
                addRenderableWidget(new ModelButton(xStart, yStart, false, Pair.of(id, models.get(id)), ClientModelManager.EXTRA_INFO.get(ModelIdUtil.getMainId(id)), player));
            }
        }
    }

    @Override
    @Keep
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fillGradient(0, 0, this.width, this.height, 0xc0101010, 0xc0101010);
    }

    @Override
    @Keep
    @SuppressWarnings("all")
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // 1. 先渲染面板背景（在 super.render 之前）
        graphics.fillGradient(x, y, x + 135, y + 235, 0xff_222222, 0xff_222222);
        graphics.fillGradient(x + 138, y, x + 420, y + 235, 0xff_222222, 0xff_222222);
        graphics.fillGradient(x + 351, y + 7, x + 352, y + 21, 0xFF_F3EFE0, 0xFF_F3EFE0);

        // 2. 调用 super.render 渲染背景和按钮
        super.render(graphics, mouseX, mouseY, partialTicks);

        // 3. 渲染其他内容
        textField.render(graphics, mouseX, mouseY, partialTicks);

        Window window = Minecraft.getInstance().getWindow();
        double scale = window.getGuiScale();
        int scissorX = (int) ((this.x + 5) * scale);
        int scissorY = (int) (window.getHeight() - ((this.y + 200) * scale));
        int scissorW = (int) (125 * scale);
        int scissorH = (int) (171 * scale);
        RenderSystem.enableScissor(scissorX, scissorY, scissorW, scissorH);
        InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, x + 5, y + 20, x + 130, y + 200, 70, 0.0625F, mouseX, mouseY, player);
        RenderSystem.disableScissor();

        var cap = player.getData(YSMAttachments.MODEL_INFO);
        String modelName = cap.getModelId().getPath();
        List<FormattedCharSequence> modelNameSplit = font.split(FormattedText.of(modelName), 125);
        int lineY = y + 205;
        for (FormattedCharSequence line : modelNameSplit) {
            int nameWidth = font.width(line);
            graphics.drawString(font, line, x + (135 - nameWidth) / 2, lineY, 0xF3EFE0);
            lineY += 10;
        }

        if (textField.getValue().isEmpty() && !textField.isFocused()) {
            graphics.drawString(font, Component.translatable("gui.legacyysm_byn.search").withStyle(ChatFormatting.ITALIC), x + 148, y + 10, 0x777777);
        }

        String pageInfo = String.format("%d/%d", page + 1, this.maxPage + 1);
        graphics.drawString(font, pageInfo, x + 138 + (282 - font.width(pageInfo)) / 2, y + 223 - font.lineHeight / 2, 0xF3EFE0);

        String debugInfo = String.format("%s-%s", SharedConstants.getCurrentVersion().getName(), ModList.get().getModFileById(YesSteveModel.MOD_ID).versionString());
        graphics.drawString(font, debugInfo, x + 2, y + 226, ChatFormatting.DARK_GRAY.getColor());

        this.renderables.stream().filter(r -> r instanceof FlatIconButton).forEach(r -> ((FlatIconButton) r).renderToolTip(graphics, this, mouseX, mouseY));
        this.renderables.stream().filter(r -> r instanceof ModelButton).forEach(r -> ((ModelButton) r).renderComponentTooltip(graphics, this, mouseX, mouseY));
    }

    @Override
    @Keep
    public void resize(Minecraft minecraft, int width, int height) {
        String value = this.textField.getValue();
        super.resize(minecraft, width, height);
        this.textField.setValue(value);
    }

    @Override
    @Keep
    public void tick() {
        // EditBox.tick() was removed in 1.20.2+
    }

    @Override
    @Keep
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.textField.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(this.textField);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    @Keep
    public boolean charTyped(char codePoint, int modifiers) {
        if (textField == null) {
            return false;
        }
        String perText = this.textField.getValue();
        if (this.textField.charTyped(codePoint, modifiers)) {
            if (!Objects.equals(perText, this.textField.getValue())) {
                this.page = 0;
                this.init();
            }
            return true;
        }
        return false;
    }

    @Override
    @Keep
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean hasKeyCode = InputConstants.getKey(keyCode, scanCode).getNumericKeyValue().isPresent();
        String preText = this.textField.getValue();
        if (hasKeyCode) {
            return true;
        }
        if (this.textField.keyPressed(keyCode, scanCode, modifiers)) {
            if (!Objects.equals(preText, this.textField.getValue())) {
                this.page = 0;
                this.init();
            }
            return true;
        } else {
            return this.textField.isFocused() && this.textField.isVisible() && keyCode != 256 || super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    @Keep
    protected void insertText(String text, boolean overwrite) {
        if (overwrite) {
            this.textField.setValue(text);
        } else {
            this.textField.insertText(text);
        }
    }

    @Override
    @Keep
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (minecraft == null) {
            return false;
        }
        double delta = scrollY;
        if (delta != 0 && inRange(mouseX, mouseY)) {
            return scrollPage(delta);
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private boolean inRange(double mouseX, double mouseY) {
        boolean isInWidthRange = (x + 143) < mouseX && mouseX < (x + 430);
        boolean isInHeightRange = (y + 25) < mouseY && mouseY < (y + 235);
        return isInWidthRange && isInHeightRange;
    }

    private boolean scrollPage(double delta) {
        if (delta > 0 && this.page > 0) {
            this.page--;
            getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            this.init();
        }
        if (delta < 0 && this.page < this.maxPage) {
            this.page++;
            getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            this.init();
        }
        return true;
    }

    @Override
    @Keep
    public boolean isPauseScreen() {
        return false;
    }

    private enum Category {
        /**
         * 不同页面类别
         */
        ALL, AUTH, STAR
    }
}
