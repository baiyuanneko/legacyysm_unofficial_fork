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
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.concurrent.ExecutionException;

@EventBusSubscriber(value = Dist.CLIENT, modid = YesSteveModel.MOD_ID)
public class ReplacePlayerHandRenderEvent {
    private static final String LEFT_ARM = "LeftArm";
    private static final String RIGHT_ARM = "RightArm";

    @SubscribeEvent
    public static void onRenderHand(RenderArmEvent event) {
        if (GeneralConfig.DISABLE_SELF_MODEL.get()) {
            return;
        }
        if (GeneralConfig.DISABLE_SELF_HANDS.get()) {
            return;
        }
        event.setCanceled(true);
        AbstractClientPlayer player = event.getPlayer();
        var cap = player.getData(YSMAttachments.MODEL_INFO);
        ResourceLocation modelId = cap.getModelId();
        GeoModel geoModel = GeckoLibCache.getInstance().getGeoModels().get(ModelIdUtil.getArmId(cap.getModelId()));
        if (geoModel == null || !hasArmBone(event.getArm(), geoModel)) {
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
                if (event.getArm() == HumanoidArm.LEFT) {
                    poseStack.pushPose();
                    poseStack.translate(0.25, 1.8, 0);
                    poseStack.scale(-1, -1, 1);
                    geoModel.getTopLevelBone(LEFT_ARM).ifPresent(bone -> instance.renderRecursively(bone, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1));
                    poseStack.popPose();
                }
                if (event.getArm() == HumanoidArm.RIGHT) {
                    poseStack.pushPose();
                    poseStack.translate(-0.25, 1.8, 0);
                    poseStack.scale(-1, -1, 1);
                    geoModel.getTopLevelBone(RIGHT_ARM).ifPresent(bone -> instance.renderRecursively(bone, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1));
                    poseStack.popPose();
                }
            }
        }
    }

    private static boolean hasArmBone(HumanoidArm arm, GeoModel model) {
        if (arm == HumanoidArm.LEFT) {
            return model.hasTopLevelBone(LEFT_ARM);
        } else {
            return model.hasTopLevelBone(RIGHT_ARM);
        }
    }
}
