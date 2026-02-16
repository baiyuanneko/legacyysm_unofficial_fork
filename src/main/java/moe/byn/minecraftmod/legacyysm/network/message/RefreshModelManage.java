package moe.byn.minecraftmod.legacyysm.network.message;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import moe.byn.minecraftmod.legacyysm.command.sub.ManageCommand;
import moe.byn.minecraftmod.legacyysm.model.ServerModelManager;
import moe.byn.minecraftmod.legacyysm.network.NetworkHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public class RefreshModelManage implements CustomPacketPayload {
    public static final RefreshModelManage INSTANCE = new RefreshModelManage();
    
    public static final CustomPacketPayload.Type<RefreshModelManage> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "refresh_model_manage"));

    public static final StreamCodec<ByteBuf, RefreshModelManage> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    public RefreshModelManage() {
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(RefreshModelManage message, IPayloadContext context) {
        context.enqueueWork(() -> {
            List<RequestServerModelInfo.Info> customInfo = ManageCommand.getFilesInfo(ServerModelManager.CUSTOM);
            List<RequestServerModelInfo.Info> authInfo = ManageCommand.getFilesInfo(ServerModelManager.AUTH);
            NetworkHandler.sendToClientPlayer(new RequestServerModelInfo(customInfo, authInfo), context.player());
        });
    }
}
