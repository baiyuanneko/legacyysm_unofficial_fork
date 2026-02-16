package moe.byn.minecraftmod.legacyysm.client.event;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import moe.byn.minecraftmod.legacyysm.capability.YSMAttachments;
import moe.byn.minecraftmod.legacyysm.client.entity.CustomPlayerEntity;
import moe.byn.minecraftmod.legacyysm.client.renderer.CustomPlayerRenderer;
import moe.byn.minecraftmod.legacyysm.config.GeneralConfig;
import moe.byn.minecraftmod.legacyysm.event.api.SpecialPlayerRenderEvent;
import moe.byn.minecraftmod.legacyysm.geckolib3.core.IAnimatable;
import moe.byn.minecraftmod.legacyysm.geckolib3.geo.render.built.GeoModel;
import moe.byn.minecraftmod.legacyysm.geckolib3.resource.GeckoLibCache;
import moe.byn.minecraftmod.legacyysm.util.AnimatableCacheUtil;
import moe.byn.minecraftmod.legacyysm.util.ModelIdUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.concurrent.ExecutionException;

@EventBusSubscriber(value = Dist.CLIENT, modid = YesSteveModel.MOD_ID)
public class RenderFirstPlayerBackground {
    private static final String NAME = "Background";
    /**
     * 因为 RenderHandEvent 可有几率会渲染多次，所以为了避免多次渲染，这样设计
     */
    private static boolean ALREADY_RENDERED = false;

    @SubscribeEvent
    public static void onRenderLevelLase(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) {
            ALREADY_RENDERED = false;
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (GeneralConfig.DISABLE_SELF_MODEL.get()) {
            return;
        }
        if (GeneralConfig.DISABLE_SELF_HANDS.get()) {
            return;
        }
        AbstractClientPlayer player = Minecraft.getInstance().player;
        if (player == null || ALREADY_RENDERED) {
            return;
        }
        ALREADY_RENDERED = true;
        var cap = player.getData(YSMAttachments.MODEL_INFO);
        ResourceLocation modelId = cap.getModelId();
        GeoModel geoModel = GeckoLibCache.getInstance().getGeoModels().get(ModelIdUtil.getArmId(cap.getModelId()));
        if (geoModel == null || !geoModel.hasTopLevelBone(NAME)) {
            return;
        }
        CustomPlayerRenderer instance = RegisterEntityRenderersEvent.getInstance();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource multiBufferSource = event.getMultiBufferSource();
        VertexConsumer buffer;
        IAnimatable animatable;

        try {
            animatable = AnimatableCacheUtil.ANIMATABLE_CACHE.get(modelId, () -> {
                CustomPlayerEntity entity = new CustomPlayerEntity();
                entity.setTexture(cap.getSelectTexture());
                return entity;
            });
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        if (animatable instanceof CustomPlayerEntity customPlayer) {
            customPlayer.setTexture(cap.getSelectTexture());
            SpecialPlayerRenderEvent renderEvent = new SpecialPlayerRenderEvent(player, customPlayer, modelId);
            NeoForge.EVENT_BUS.post(renderEvent);
            if (renderEvent.isCanceled()) {
                return;
            }
            buffer = multiBufferSource.getBuffer(RenderType.entityTranslucent(customPlayer.getTexture()));
            int packedLight = event.getPackedLight();
            if (instance != null) {
                poseStack.pushPose();
                if (Minecraft.getInstance().options.bobView().get()) {
                    bobView(poseStack, event.getPartialTick(), player);
                }
                poseStack.translate(0, -1.5, 0);
                geoModel.getTopLevelBone(NAME).ifPresent(bone -> instance.renderRecursively(bone, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1));
                poseStack.popPose();
            }
        }
    }

    private static void bobView(PoseStack pMatrixStack, float pPartialTicks, Player player) {
        float walk = player.walkDist - player.walkDistO;
        float walk2 = -(player.walkDist + walk * pPartialTicks);
        float lerp = Mth.lerp(pPartialTicks, player.oBob, player.bob);
        pMatrixStack.translate(-Mth.sin(walk2 * (float) Math.PI) * lerp * 0.5F, Math.abs(Mth.cos(walk2 * (float) Math.PI) * lerp), 0.0D);
        pMatrixStack.mulPose(Axis.ZN.rotationDegrees(Mth.sin(walk2 * (float) Math.PI) * lerp * 3.0F));
        pMatrixStack.mulPose(Axis.XN.rotationDegrees(Math.abs(Mth.cos(walk2 * (float) Math.PI - 0.2F) * lerp) * 5.0F));
    }
}
