package moe.byn.minecraftmod.legacyysm.client.model;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import moe.byn.minecraftmod.legacyysm.client.animation.AnimationRegister;
import moe.byn.minecraftmod.legacyysm.client.compat.FirstPersonCompat;
import moe.byn.minecraftmod.legacyysm.client.entity.CustomPlayerEntity;
import moe.byn.minecraftmod.legacyysm.geckolib3.core.IAnimatable;
import moe.byn.minecraftmod.legacyysm.geckolib3.core.event.predicate.AnimationEvent;
import moe.byn.minecraftmod.legacyysm.geckolib3.core.molang.MolangParser;
import moe.byn.minecraftmod.legacyysm.geckolib3.core.processor.IBone;
import moe.byn.minecraftmod.legacyysm.geckolib3.geo.render.built.GeoBone;
import moe.byn.minecraftmod.legacyysm.geckolib3.model.AnimatedGeoModel;
import moe.byn.minecraftmod.legacyysm.geckolib3.model.provider.data.EntityModelData;
import moe.byn.minecraftmod.legacyysm.geckolib3.resource.GeckoLibCache;
import moe.byn.minecraftmod.legacyysm.util.Keep;
import moe.byn.minecraftmod.legacyysm.util.ModelIdUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;

import javax.annotation.Nullable;
import java.util.List;

@SuppressWarnings("all")
public class CustomPlayerModel extends AnimatedGeoModel {
    public static final ResourceLocation DEFAULT_MAIN_MODEL = ModelIdUtil.getMainId(ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "default"));
    public static final ResourceLocation DEFAULT_MAIN_ANIMATION = ModelIdUtil.getMainId(ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "default"));
    public static final ResourceLocation DEFAULT_TEXTURE = ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "default/default.png");
    public static final String FIRST_PERSON_MOD_ID = "firstpersonmod";
    public static float FIRST_PERSON_HEAD_POS;

    @Override
    @Keep
    public ResourceLocation getModelLocation(Object object) {
        if (object instanceof CustomPlayerEntity customPlayer) {
            return customPlayer.getMainModel();
        }
        return DEFAULT_MAIN_MODEL;
    }

    @Override
    @Keep
    public ResourceLocation getTextureLocation(Object object) {
        if (object instanceof CustomPlayerEntity customPlayer) {
            return customPlayer.getTexture();
        }
        return DEFAULT_TEXTURE;
    }

    @Override
    @Keep
    public ResourceLocation getAnimationFileLocation(Object object) {
        if (object instanceof CustomPlayerEntity customPlayer) {
            return customPlayer.getAnimation();
        }
        return DEFAULT_MAIN_ANIMATION;
    }

    @Override
    @Keep
    public void setCustomAnimations(IAnimatable animatable, int instanceId, AnimationEvent animationEvent) {
        List extraData = animationEvent.getExtraData();
        MolangParser parser = GeckoLibCache.getInstance().parser;
        if (!Minecraft.getInstance().isPaused() && extraData.size() == 1 && extraData.get(0) instanceof EntityModelData data
                && animatable instanceof CustomPlayerEntity customPlayer && customPlayer.getPlayer() != null) {
            Player player = customPlayer.getPlayer();
            AnimationRegister.setParserValue(animationEvent, parser, data, player);
            super.setCustomAnimations(animatable, instanceId, animationEvent);
            this.codeAnimation(animationEvent, data, player);
        } else {
            super.setCustomAnimations(animatable, instanceId, animationEvent);
        }
    }

    private void codeAnimation(AnimationEvent animationEvent, EntityModelData data, Player player) {
        // FIXME: 2023/6/21 这一块设计应该改成 molang 的，而且这个寻找效率低下
        IBone head = getBone("Head");
        FIRST_PERSON_HEAD_POS = 24;
        if (head != null) {
            head.setRotationX(head.getRotationX() + (float) Math.toRadians(data.headPitch));
            head.setRotationY(head.getRotationY() + (float) Math.toRadians(data.netHeadYaw));
            FIRST_PERSON_HEAD_POS = head.getPivotY() * ((CustomPlayerEntity) animationEvent.getAnimatable()).getHeightScale();
        }
        if (getCurrentModel().firstPersonViewLocator != null) {
            float heightScale = ((CustomPlayerEntity) animationEvent.getAnimatable()).getHeightScale();
            GeoBone locator = getCurrentModel().firstPersonViewLocator;
            FIRST_PERSON_HEAD_POS = locator.getPivotY() * heightScale;
        }
        if (ModList.get().isLoaded(FIRST_PERSON_MOD_ID) && getCurrentModel().firstPersonHead != null) {
            FirstPersonCompat.hideHead(getCurrentModel().firstPersonHead);
        }
    }

    @Override
    @Keep
    @Nullable
    public IBone getBone(String boneName) {
        return getAnimationProcessor().getBone(boneName);
    }

    @Override
    @Keep
    public void setMolangQueries(IAnimatable animatable, double seekTime) {
    }
}
