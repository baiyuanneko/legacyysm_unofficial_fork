package moe.byn.minecraftmod.legacyysm.network.message;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import moe.byn.minecraftmod.legacyysm.model.ServerModelManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public record HandleFile(String name, UploadFile.Dir dir, String action, String rename) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<HandleFile> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "handle_file")
            );

    public static final StreamCodec<ByteBuf, HandleFile> STREAM_CODEC =
            StreamCodec.of(
                    HandleFile::encode,
                    HandleFile::decode
            );

    private static void encode(ByteBuf buf, HandleFile msg) {
        byte[] nameBytes = msg.name.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        buf.writeInt(nameBytes.length);
        buf.writeBytes(nameBytes);
        buf.writeInt(msg.dir.ordinal());
        byte[] actionBytes = msg.action.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        buf.writeInt(actionBytes.length);
        buf.writeBytes(actionBytes);
        byte[] renameBytes = msg.rename.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        buf.writeInt(renameBytes.length);
        buf.writeBytes(renameBytes);
    }

    private static HandleFile decode(ByteBuf buf) {
        byte[] nameBytes = new byte[buf.readInt()];
        buf.readBytes(nameBytes);
        String name = new String(nameBytes, java.nio.charset.StandardCharsets.UTF_8);
        UploadFile.Dir dir = UploadFile.Dir.values()[buf.readInt()];
        byte[] actionBytes = new byte[buf.readInt()];
        buf.readBytes(actionBytes);
        String action = new String(actionBytes, java.nio.charset.StandardCharsets.UTF_8);
        byte[] renameBytes = new byte[buf.readInt()];
        buf.readBytes(renameBytes);
        String rename = new String(renameBytes, java.nio.charset.StandardCharsets.UTF_8);
        return new HandleFile(name, dir, action, rename);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(final HandleFile data, final IPayloadContext context) {
        if (context.player() instanceof ServerPlayer serverPlayer && serverPlayer.hasPermissions(4)) {
            handleFileOperation(data);
        }
    }

    private static void handleFileOperation(HandleFile data) {
        if (data.dir == UploadFile.Dir.CUSTOM) {
            processFile(data, ServerModelManager.CUSTOM, ServerModelManager.AUTH);
        }

        if (data.dir == UploadFile.Dir.AUTH) {
            processFile(data, ServerModelManager.AUTH, ServerModelManager.CUSTOM);
        }
    }

    private static void processFile(HandleFile data, Path sourceDir, Path destDir) {
        File file = sourceDir.resolve(data.name).toFile();
        if (!FileUtils.isRegularFile(file) && !FileUtils.isDirectory(file)) {
            return;
        }

        if (data.action.equals("delete")) {
            FileUtils.deleteQuietly(file);
        }

        if (data.action.equals("move")) {
            File destFile = destDir.resolve(data.name).toFile();
            moveFile(file, destFile);
        }

        if (data.action.equals("rename") && StringUtils.isNotBlank(data.rename)) {
            File destFile = sourceDir.resolve(data.rename).toFile();
            moveFile(file, destFile);
        }
    }

    private static void moveFile(File source, File dest) {
        try {
            if (FileUtils.isRegularFile(source)) {
                FileUtils.moveFile(source, dest);
            }
            if (FileUtils.isDirectory(source)) {
                FileUtils.moveDirectory(source, dest);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
