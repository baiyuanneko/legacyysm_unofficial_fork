package moe.byn.minecraftmod.legacyysm.network.message;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import moe.byn.minecraftmod.legacyysm.data.EncryptTools;
import moe.byn.minecraftmod.legacyysm.model.format.ServerModelInfo;
import moe.byn.minecraftmod.legacyysm.network.NetworkHandler;
import moe.byn.minecraftmod.legacyysm.util.ThreadTools;
import moe.byn.minecraftmod.legacyysm.util.UuidUtils;
import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static moe.byn.minecraftmod.legacyysm.model.ServerModelManager.*;

public class SyncModelFiles implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncModelFiles> TYPE = 
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "sync_model_files"));
    
    public static final StreamCodec<ByteBuf, SyncModelFiles> STREAM_CODEC = StreamCodec.of(
            SyncModelFiles::encode,
            SyncModelFiles::decode
    );
    
    private static void encode(ByteBuf buf, SyncModelFiles msg) {
        FriendlyByteBuf friendlyBuf = new FriendlyByteBuf(buf);
        friendlyBuf.writeVarInt(msg.md5Info.length);
        for (String md5 : msg.md5Info) {
            friendlyBuf.writeUtf(md5);
        }
    }
    
    private static SyncModelFiles decode(ByteBuf buf) {
        FriendlyByteBuf friendlyBuf = new FriendlyByteBuf(buf);
        int count = friendlyBuf.readVarInt();
        String[] output = new String[count];
        for (int i = 0; i < count; i++) {
            output[i] = friendlyBuf.readUtf();
        }
        return new SyncModelFiles(output);
    }

    private final String[] md5Info;

    public SyncModelFiles(String[] md5Info) {
        this.md5Info = md5Info;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(SyncModelFiles message, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer sender = (ServerPlayer) context.player();
            if (sender == null) {
                return;
            }
            sendPassword(sender);
            sendModelFiles(message.md5Info, sender);
        });
    }

    private static void sendModelFiles(String[] md5Info, ServerPlayer sender) {
        Collection<String> cache = CACHE_NAME_INFO.values().stream().map(ServerModelInfo::getMd5).toList();
        List<String> output = Lists.newArrayList(cache);
        for (String md5 : md5Info) {
            if (cache.contains(md5)) {
                output.remove(md5);
                NetworkHandler.sendToClientPlayer(new RequestLoadModel(md5), sender);
            }
        }
        for (String md5 : output) {
            File modelFile = CACHE_SERVER.resolve(md5).toFile();
            try {
                byte[] modelBytes = FileUtils.readFileToByteArray(modelFile);
                ThreadTools.THREAD_POOL.submit(() -> NetworkHandler.sendToClientPlayer(new SendModelFile(modelBytes), sender));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void sendPassword(ServerPlayer sender) {
        try {
            byte[] password = FileUtils.readFileToByteArray(PASSWORD_FILE.toFile());
            byte[] uuid = UuidUtils.asBytes(sender.getUUID());
            byte[] output = EncryptTools.encryptPassword(uuid, password);
            ThreadTools.THREAD_POOL.submit(() -> NetworkHandler.sendToClientPlayer(new SendModelFile(output), sender));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
