package moe.byn.minecraftmod.legacyysm.client.renderer;

import moe.byn.minecraftmod.legacyysm.bukkit.client.NPCData;
import moe.byn.minecraftmod.legacyysm.capability.YSMAttachments;
import moe.byn.minecraftmod.legacyysm.client.entity.CustomPlayerEntity;
import moe.byn.minecraftmod.legacyysm.client.model.CustomPlayerModel;
import moe.byn.minecraftmod.legacyysm.client.renderer.layer.CustomPlayerElytraLayer;
import moe.byn.minecraftmod.legacyysm.client.renderer.layer.CustomPlayerItemInHandLayer;
import moe.byn.minecraftmod.legacyysm.event.api.SpecialPlayerRenderEvent;
import moe.byn.minecraftmod.legacyysm.geckolib3.geo.GeoReplacedEntityRenderer;
import moe.byn.minecraftmod.legacyysm.geckolib3.geo.render.built.GeoModel;
import moe.byn.minecraftmod.legacyysm.geckolib3.resource.GeckoLibCache;
import moe.byn.minecraftmod.legacyysm.util.Keep;
import moe.byn.minecraftmod.legacyysm.util.ModelIdUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

public class CustomPlayerRenderer extends GeoReplacedEntityRenderer<CustomPlayerEntity> {
    private GeoModel geoModel;

    @SuppressWarnings("all")
    public CustomPlayerRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new CustomPlayerModel(), new CustomPlayerEntity());
        addLayer(new CustomPlayerItemInHandLayer<>(this, ctx.getItemInHandRenderer()));
        addLayer(new CustomPlayerElytraLayer<>(this, ctx.getModelSet()));
    }

    @Override
    @Keep
    public void render(Entity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (this.animatable != null && entity instanceof Player player) {
            var cap = player.getData(YSMAttachments.MODEL_INFO);
            this.animatable.setPlayer(player);
            if (NPCData.contains(player.getUUID())) {
                Pair<ResourceLocation, ResourceLocation> data = NPCData.getData(player.getUUID());
                this.animatable.setMainModel(ModelIdUtil.getMainId(data.left()));
                this.animatable.setTexture(data.right());
            } else {
                this.animatable.setMainModel(ModelIdUtil.getMainId(cap.getModelId()));
                this.animatable.setTexture(cap.getSelectTexture());
            }
            SpecialPlayerRenderEvent renderEvent = new SpecialPlayerRenderEvent(player, this.animatable, ModelIdUtil.getModelIdFromMainId(this.animatable.getMainModel()));
            NeoForge.EVENT_BUS.post(renderEvent);
            if (renderEvent.isCanceled()) {
                return;
            }
        }
        ResourceLocation location = this.modelProvider.getModelLocation(animatable);
        GeoModel geoModel = GeckoLibCache.getInstance().getGeoModels().get(location);
        if (geoModel != null) {
            this.geoModel = geoModel;
            super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        }
    }

    @Override
    @Keep
    public RenderType getRenderType(Object animatable, float partialTick, PoseStack poseStack, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, int packedLight, ResourceLocation texture) {
        return RenderType.entityTranslucent(texture);
    }

    @Override
    @Keep
    public boolean shouldShowName(Entity entity) {
        double distance = this.entityRenderDispatcher.distanceToSqr(entity);
        float renderDistance = entity.isDiscrete() ? 32.0F : 64.0F;
        if (distance >= (double) (renderDistance * renderDistance)) {
            return false;
        } else {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer player = minecraft.player;
            if (player == null) {
                return false;
            }
            boolean invisible = !entity.isInvisibleTo(player);
            if (entity != player) {
                Team team1 = entity.getTeam();
                Team team2 = player.getTeam();
                if (team1 != null) {
                    Team.Visibility team$visibility = team1.getNameTagVisibility();
                    return switch (team$visibility) {
                        case ALWAYS -> invisible;
                        case NEVER -> false;
                        case HIDE_FOR_OTHER_TEAMS ->
                                team2 == null ? invisible : team1.isAlliedTo(team2) && (team1.canSeeFriendlyInvisibles() || invisible);
                        case HIDE_FOR_OWN_TEAM -> team2 == null ? invisible : !team1.isAlliedTo(team2) && invisible;
                    };
                }
            }
            return Minecraft.renderNames() && entity != minecraft.getCameraEntity() && invisible && !entity.isVehicle();
        }
    }

    @Override
    @Keep
    @SuppressWarnings("all")
    protected void renderNameTag(Entity entity, Component displayName, PoseStack poseStack, MultiBufferSource buffer, int packedLight, float partialTick) {
        double distance = this.entityRenderDispatcher.distanceToSqr(entity);
        poseStack.pushPose();
        if (distance < 100 && entity instanceof Player player) {
            Scoreboard scoreboard = player.getScoreboard();
            Objective objective = scoreboard.getDisplayObjective(DisplaySlot.BELOW_NAME);
            if (objective != null) {
                ScoreHolder scoreHolder = ScoreHolder.forNameOnly(player.getScoreboardName());
                var score = scoreboard.getPlayerScoreInfo(scoreHolder, objective);
                String scoreText = score != null ? String.valueOf(score.value()) : "0";
                super.renderNameTag(entity, Component.literal(scoreText).append(" ").append(objective.getDisplayName()), poseStack, buffer, packedLight, partialTick);
                poseStack.translate(0, 9.0 * 1.15 * 0.025, 0);
            }
        }
        super.renderNameTag(entity, displayName, poseStack, buffer, packedLight, partialTick);
        poseStack.popPose();
    }

    @Override
    @Keep
    public float getWidthScale(Object animatable) {
        if (this.animatable != null) {
            return this.animatable.getWidthScale();
        }
        return super.getWidthScale(animatable);
    }

    @Override
    @Keep
    public float getHeightScale(Object animatable) {
        if (this.animatable != null) {
            return this.animatable.getHeightScale();
        }
        return super.getHeightScale(animatable);
    }

    public CustomPlayerEntity getCustomPlayerEntity() {
        return this.animatable;
    }

    @Nullable
    public GeoModel getGeoModel() {
        return geoModel;
    }
}
