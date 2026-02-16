package moe.byn.minecraftmod.legacyysm.network.message;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import moe.byn.minecraftmod.legacyysm.model.ServerModelManager;
import moe.byn.minecraftmod.legacyysm.network.NetworkHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;

public class UploadFile implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UploadFile> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "upload_file"));

    public static final StreamCodec<ByteBuf, UploadFile> STREAM_CODEC = StreamCodec.of(
            UploadFile::encode,
            UploadFile::decode
    );

    private final String name;
    private final byte[] fileBytes;
    private final Dir dir;

    public UploadFile(String name, byte[] fileBytes, Dir dir) {
        this.name = name;
        this.fileBytes = fileBytes;
        this.dir = dir;
    }

    private static void encode(ByteBuf buf, UploadFile message) {
        FriendlyByteBuf friendlyBuf = new FriendlyByteBuf(buf);
        friendlyBuf.writeUtf(message.name);
        friendlyBuf.writeByteArray(message.fileBytes);
        friendlyBuf.writeEnum(message.dir);
    }

    private static UploadFile decode(ByteBuf buf) {
        FriendlyByteBuf friendlyBuf = new FriendlyByteBuf(buf);
        String name = friendlyBuf.readUtf();
        byte[] bytes = friendlyBuf.readByteArray();
        Dir dirOut = friendlyBuf.readEnum(Dir.class);
        return new UploadFile(name, bytes, dirOut);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(UploadFile message, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player && player.hasPermissions(4)) {
            context.enqueueWork(() -> writeFile(message, player));
        }
    }

    private static void writeFile(UploadFile message, ServerPlayer player) {
        Path filePath;
        if (message.dir == Dir.CUSTOM) {
            filePath = ServerModelManager.CUSTOM.resolve(message.name);
        } else {
            filePath = ServerModelManager.AUTH.resolve(message.name);
        }
        try {
            FileUtils.writeByteArrayToFile(filePath.toFile(), message.fileBytes);
            NetworkHandler.sendToClientPlayer(CompleteFeedback.INSTANCE, player);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public enum Dir {
        CUSTOM,
        AUTH
    }
}
