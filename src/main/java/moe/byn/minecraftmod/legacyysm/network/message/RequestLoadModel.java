package moe.byn.minecraftmod.legacyysm.network.message;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import moe.byn.minecraftmod.legacyysm.client.ClientModelManager;
import moe.byn.minecraftmod.legacyysm.data.EncryptTools;
import moe.byn.minecraftmod.legacyysm.data.ModelData;
import moe.byn.minecraftmod.legacyysm.model.ServerModelManager;
import moe.byn.minecraftmod.legacyysm.util.ThreadTools;
import moe.byn.minecraftmod.legacyysm.util.UuidUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.apache.commons.io.FileUtils;

import java.nio.file.Path;
import java.util.UUID;

public class RequestLoadModel implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RequestLoadModel> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "request_load_model"));

    public static final StreamCodec<ByteBuf, RequestLoadModel> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, msg -> msg.fileName,
                    RequestLoadModel::new
            );

    private final String fileName;

    public RequestLoadModel(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(RequestLoadModel message, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientModelManager.CACHE_MD5.add(message.fileName);
            loadModel(message.fileName);
        });
    }

    public static void loadModel(String fileName) {
        ThreadTools.THREAD_POOL.submit(() -> {
            try {
                while (ClientModelManager.PASSWORD == null) {
                    Thread.sleep(500);
                }
                if (Minecraft.getInstance().player != null) {
                    UUID uuid = Minecraft.getInstance().player.getUUID();
                    Path modelFile = ServerModelManager.CACHE_CLIENT.resolve(fileName);
                    byte[] fileBytes = FileUtils.readFileToByteArray(modelFile.toFile());
                    ModelData data = EncryptTools.decryptModel(UuidUtils.asBytes(uuid), ClientModelManager.PASSWORD, fileBytes);
                    if (data != null) {
                        Minecraft.getInstance().tell(() -> ClientModelManager.registerAll(data));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
