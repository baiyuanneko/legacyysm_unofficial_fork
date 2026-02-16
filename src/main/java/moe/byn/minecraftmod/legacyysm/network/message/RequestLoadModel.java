package moe.byn.minecraftmod.legacyysm.network.message;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import moe.byn.minecraftmod.legacyysm.data.EncryptTools;
import moe.byn.minecraftmod.legacyysm.data.ModelData;
import moe.byn.minecraftmod.legacyysm.model.ServerModelManager;
import moe.byn.minecraftmod.legacyysm.util.ThreadTools;
import moe.byn.minecraftmod.legacyysm.util.UuidUtils;
import io.netty.buffer.ByteBuf;
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
            addToClientCacheMd5(message.fileName);
            loadModel(message.fileName);
        });
    }
    
    private static void addToClientCacheMd5(String fileName) {
        try {
            Class<?> clientModelManagerClass = Class.forName("moe.byn.minecraftmod.legacyysm.client.ClientModelManager");
            java.lang.reflect.Field cacheMd5Field = clientModelManagerClass.getDeclaredField("CACHE_MD5");
            @SuppressWarnings("unchecked")
            java.util.List<String> cacheMd5 = (java.util.List<String>) cacheMd5Field.get(null);
            cacheMd5.add(fileName);
        } catch (Exception e) {
            YesSteveModel.LOGGER.error("Failed to add to client cache MD5", e);
        }
    }

    public static void loadModel(String fileName) {
        ThreadTools.THREAD_POOL.submit(() -> {
            try {
                byte[] password = waitForClientPassword();
                if (password == null) return;
                
                Object minecraft = getMinecraftInstance();
                if (minecraft == null) return;
                
                Object player = getPlayerFromMinecraft(minecraft);
                if (player == null) return;
                
                UUID uuid = getPlayerUUID(player);
                Path modelFile = ServerModelManager.CACHE_CLIENT.resolve(fileName);
                byte[] fileBytes = FileUtils.readFileToByteArray(modelFile.toFile());
                ModelData data = EncryptTools.decryptModel(UuidUtils.asBytes(uuid), password, fileBytes);
                if (data != null) {
                    registerModelOnMainThread(minecraft, data);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    private static byte[] waitForClientPassword() {
        try {
            Class<?> clientModelManagerClass = Class.forName("moe.byn.minecraftmod.legacyysm.client.ClientModelManager");
            java.lang.reflect.Field passwordField = clientModelManagerClass.getDeclaredField("PASSWORD");
            int time = 0;
            while (passwordField.get(null) == null && time < 10) {
                Thread.sleep(500);
                time++;
            }
            return (byte[]) passwordField.get(null);
        } catch (Exception e) {
            return null;
        }
    }
    
    private static Object getMinecraftInstance() {
        try {
            Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
            java.lang.reflect.Method getInstanceMethod = minecraftClass.getMethod("getInstance");
            return getInstanceMethod.invoke(null);
        } catch (Exception e) {
            return null;
        }
    }
    
    private static Object getPlayerFromMinecraft(Object minecraft) {
        try {
            Class<?> minecraftClass = minecraft.getClass();
            java.lang.reflect.Field playerField = minecraftClass.getDeclaredField("player");
            return playerField.get(minecraft);
        } catch (Exception e) {
            return null;
        }
    }
    
    private static UUID getPlayerUUID(Object player) {
        try {
            Class<?> playerClass = player.getClass();
            java.lang.reflect.Method getUUIDMethod = playerClass.getMethod("getUUID");
            return (UUID) getUUIDMethod.invoke(player);
        } catch (Exception e) {
            return null;
        }
    }
    
    private static void registerModelOnMainThread(Object minecraft, ModelData data) {
        try {
            Class<?> minecraftClass = minecraft.getClass();
            java.lang.reflect.Method tellMethod = minecraftClass.getMethod("tell", Runnable.class);
            
            Class<?> clientModelManagerClass = Class.forName("moe.byn.minecraftmod.legacyysm.client.ClientModelManager");
            java.lang.reflect.Method registerAllMethod = clientModelManagerClass.getMethod("registerAll", ModelData.class);
            
            final Object finalData = data;
            final java.lang.reflect.Method finalRegisterMethod = registerAllMethod;
            
            tellMethod.invoke(minecraft, (Runnable) () -> {
                try {
                    finalRegisterMethod.invoke(null, finalData);
                } catch (Exception e) {
                    YesSteveModel.LOGGER.error("Failed to register model", e);
                }
            });
        } catch (Exception e) {
            YesSteveModel.LOGGER.error("Failed to schedule model registration", e);
        }
    }
}
