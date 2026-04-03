package moe.byn.minecraftmod.legacyysm.network;

import moe.byn.minecraftmod.legacyysm.bukkit.message.OpenModelGuiMessage;
import moe.byn.minecraftmod.legacyysm.bukkit.message.SetNpcModelAndTexture;
import moe.byn.minecraftmod.legacyysm.bukkit.message.SyncNpcDataMessage;
import moe.byn.minecraftmod.legacyysm.bukkit.message.UpdateNpcDataMessage;
import moe.byn.minecraftmod.legacyysm.network.message.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class NetworkHandler {

    private NetworkHandler() {}

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registerMainMessages(registrar);
        registerBukkitMessages(registrar);
    }

    private static void registerMainMessages(PayloadRegistrar registrar) {
        registrar.playToServer(SyncModelFiles.TYPE, SyncModelFiles.STREAM_CODEC, SyncModelFiles::handleServer);
        registrar.playToClient(SendModelFile.TYPE, SendModelFile.STREAM_CODEC, SendModelFile::handleClient);
        registrar.playToClient(RequestSyncModel.TYPE, RequestSyncModel.STREAM_CODEC, RequestSyncModel::handleClient);
        registrar.playToClient(RequestLoadModel.TYPE, RequestLoadModel.STREAM_CODEC, RequestLoadModel::handleClient);
        registrar.playToClient(SyncModelInfo.TYPE, SyncModelInfo.STREAM_CODEC, SyncModelInfo::handleClient);
        registrar.playToServer(SetModelAndTexture.TYPE, SetModelAndTexture.STREAM_CODEC, SetModelAndTexture::handleServer);
        registrar.playToClient(SyncAuthModels.TYPE, SyncAuthModels.STREAM_CODEC, SyncAuthModels::handleClient);
        registrar.playToServer(SetPlayAnimation.TYPE, SetPlayAnimation.STREAM_CODEC, SetPlayAnimation::handleServer);
        registrar.playToClient(SyncStarModels.TYPE, SyncStarModels.STREAM_CODEC, SyncStarModels::handleClient);
        registrar.playToServer(SetStarModel.TYPE, SetStarModel.STREAM_CODEC, SetStarModel::handleServer);
        registrar.playToClient(RequestServerModelInfo.TYPE, RequestServerModelInfo.STREAM_CODEC, RequestServerModelInfo::handleClient);
        registrar.playToServer(UploadFile.TYPE, UploadFile.STREAM_CODEC, UploadFile::handleServer);
        registrar.playToClient(CompleteFeedback.TYPE, CompleteFeedback.STREAM_CODEC, CompleteFeedback::handleClient);
        registrar.playToServer(RefreshModelManage.TYPE, RefreshModelManage.STREAM_CODEC, RefreshModelManage::handleServer);
        registrar.playToServer(HandleFile.TYPE, HandleFile.STREAM_CODEC, HandleFile::handleServer);
        registrar.playToClient(SyncServerConfig.TYPE, SyncServerConfig.STREAM_CODEC, SyncServerConfig::handleClient);
        registrar.playToServer(UploadPlayerModel.TYPE, UploadPlayerModel.STREAM_CODEC, UploadPlayerModel::handleServer);
        registrar.playToClient(SyncPlayerModel.TYPE, SyncPlayerModel.STREAM_CODEC, SyncPlayerModel::handleClient);
        registrar.playToClient(RequestUploadModel.TYPE, RequestUploadModel.STREAM_CODEC, RequestUploadModel::handleClient);
    }

    private static void registerBukkitMessages(PayloadRegistrar registrar) {
        registrar.playToClient(OpenModelGuiMessage.TYPE, OpenModelGuiMessage.STREAM_CODEC, OpenModelGuiMessage::handleClient);
        registrar.playToServer(SetNpcModelAndTexture.TYPE, SetNpcModelAndTexture.STREAM_CODEC, SetNpcModelAndTexture::handleServer);
        registrar.playToClient(SyncNpcDataMessage.TYPE, SyncNpcDataMessage.STREAM_CODEC, SyncNpcDataMessage::handleClient);
        registrar.playToClient(UpdateNpcDataMessage.TYPE, UpdateNpcDataMessage.STREAM_CODEC, UpdateNpcDataMessage::handleClient);
    }

    public static <T extends CustomPacketPayload> void sendToClientPlayer(T payload, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, payload);
        }
    }

    public static <T extends CustomPacketPayload> void sendToServer(T payload) {
        PacketDistributor.sendToServer(payload);
    }

    public static <T extends CustomPacketPayload> void sendToAllPlayers(T payload) {
        PacketDistributor.sendToAllPlayers(payload);
    }
}
