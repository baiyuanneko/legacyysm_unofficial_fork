package moe.byn.minecraftmod.legacyysm.network.message;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import moe.byn.minecraftmod.legacyysm.capability.YSMAttachments;
import moe.byn.minecraftmod.legacyysm.network.NetworkHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetPlayAnimation(int extraAnimationId) implements CustomPacketPayload {

    private static final int STOP = -1;

    public static final CustomPacketPayload.Type<SetPlayAnimation> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "set_play_animation")
            );

    public static final StreamCodec<ByteBuf, SetPlayAnimation> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, SetPlayAnimation::extraAnimationId,
                    SetPlayAnimation::new
            );

    public static SetPlayAnimation stop() {
        return new SetPlayAnimation(STOP);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(final SetPlayAnimation data, final IPayloadContext context) {
        if (STOP <= data.extraAnimationId && data.extraAnimationId < 8) {
            handleCapability(data, context);
        }
    }

    private static void handleCapability(SetPlayAnimation data, IPayloadContext context) {
        var player = context.player();
        var modelIdCap = player.getData(YSMAttachments.MODEL_INFO);
        if (data.extraAnimationId == STOP) {
            modelIdCap.stopAnimation();
        } else {
            modelIdCap.playAnimation("extra" + data.extraAnimationId);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            SyncModelInfo syncMsg = new SyncModelInfo(serverPlayer.getId(), modelIdCap);
            NetworkHandler.sendToAllPlayers(syncMsg);
        }
    }
}
