package moe.byn.minecraftmod.legacyysm.network.message;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import moe.byn.minecraftmod.legacyysm.client.gui.ModelManageScreen;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public class RequestServerModelInfo implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RequestServerModelInfo> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "request_server_model_info"));

    public static final StreamCodec<ByteBuf, RequestServerModelInfo> STREAM_CODEC = StreamCodec.of(
            RequestServerModelInfo::encode,
            RequestServerModelInfo::decode
    );

    private final List<Info> customModels;
    private final List<Info> authModels;

    public RequestServerModelInfo(List<Info> customModels, List<Info> authModels) {
        this.customModels = customModels;
        this.authModels = authModels;
    }

    private static void encode(ByteBuf buf, RequestServerModelInfo message) {
        FriendlyByteBuf friendlyBuf = new FriendlyByteBuf(buf);
        friendlyBuf.writeVarInt(message.customModels.size());
        for (Info info : message.customModels) {
            encodeInfo(friendlyBuf, info);
        }
        friendlyBuf.writeVarInt(message.authModels.size());
        for (Info info : message.authModels) {
            encodeInfo(friendlyBuf, info);
        }
    }

    private static RequestServerModelInfo decode(ByteBuf buf) {
        FriendlyByteBuf friendlyBuf = new FriendlyByteBuf(buf);
        List<Info> outCustomModels = new ArrayList<>();
        List<Info> outAuthModels = new ArrayList<>();
        int customModelsSize = friendlyBuf.readVarInt();
        for (int i = 0; i < customModelsSize; i++) {
            outCustomModels.add(decodeInfo(friendlyBuf));
        }
        int authModelsSize = friendlyBuf.readVarInt();
        for (int i = 0; i < authModelsSize; i++) {
            outAuthModels.add(decodeInfo(friendlyBuf));
        }
        return new RequestServerModelInfo(outCustomModels, outAuthModels);
    }

    private static void encodeInfo(FriendlyByteBuf buf, Info info) {
        buf.writeUtf(info.fileName);
        buf.writeEnum(info.modelType);
        buf.writeLong(info.size);
    }

    private static Info decodeInfo(FriendlyByteBuf buf) {
        return new Info(buf.readUtf(), buf.readEnum(moe.byn.minecraftmod.legacyysm.model.format.Type.class), buf.readLong());
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(RequestServerModelInfo message, IPayloadContext context) {
        context.enqueueWork(() -> openGui(message));
    }

    private static void openGui(RequestServerModelInfo message) {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new ModelManageScreen(message.customModels, message.authModels));
    }

    public static class Info {
        private String fileName;
        private moe.byn.minecraftmod.legacyysm.model.format.Type modelType;
        private long size;

        public Info(String fileName, moe.byn.minecraftmod.legacyysm.model.format.Type modelType, long size) {
            this.fileName = fileName;
            this.modelType = modelType;
            this.size = size;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public moe.byn.minecraftmod.legacyysm.model.format.Type getType() {
            return modelType;
        }

        public void setType(moe.byn.minecraftmod.legacyysm.model.format.Type modelType) {
            this.modelType = modelType;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }
    }
}
