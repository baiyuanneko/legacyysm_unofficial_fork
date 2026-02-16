package moe.byn.minecraftmod.legacyysm.network.message;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import moe.byn.minecraftmod.legacyysm.client.ClientModelManager;
import moe.byn.minecraftmod.legacyysm.model.ServerModelManager;
import moe.byn.minecraftmod.legacyysm.util.Md5Utils;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class SendModelFile implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SendModelFile> TYPE = 
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "send_model_file"));
    
    public static final StreamCodec<ByteBuf, SendModelFile> STREAM_CODEC = StreamCodec.of(
            SendModelFile::encode,
            SendModelFile::decode
    );
    
    private static void encode(ByteBuf buf, SendModelFile msg) {
        new FriendlyByteBuf(buf).writeByteArray(msg.data);
    }
    
    private static SendModelFile decode(ByteBuf buf) {
        return new SendModelFile(new FriendlyByteBuf(buf).readByteArray());
    }

    private final byte[] data;

    public SendModelFile(byte[] data) {
        this.data = data;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(SendModelFile message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (message.data.length == 48) {
                ClientModelManager.PASSWORD = message.data;
            } else {
                String fileName = Md5Utils.md5Hex(message.data).toUpperCase(Locale.US);
                File file = ServerModelManager.CACHE_CLIENT.resolve(fileName).toFile();
                try {
                    FileUtils.writeByteArrayToFile(file, message.data);
                    RequestLoadModel.loadModel(fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
