package moe.byn.minecraftmod.legacyysm.network.message;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import moe.byn.minecraftmod.legacyysm.capability.YSMAttachments;
import moe.byn.minecraftmod.legacyysm.client.ClientModelManager;
import moe.byn.minecraftmod.legacyysm.data.ModelData;
import moe.byn.minecraftmod.legacyysm.model.format.Type;
import moe.byn.minecraftmod.legacyysm.util.ModelIdUtil;
import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.UUID;

public class SyncPlayerModel implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncPlayerModel> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "sync_player_model"));

    public static final StreamCodec<ByteBuf, SyncPlayerModel> STREAM_CODEC = StreamCodec.of(
            SyncPlayerModel::encode,
            SyncPlayerModel::decode
    );

    private static void encode(ByteBuf buf, SyncPlayerModel msg) {
        FriendlyByteBuf friendlyBuf = new FriendlyByteBuf(buf);
        friendlyBuf.writeUUID(msg.playerUuid);
        friendlyBuf.writeUtf(msg.modelId);
        friendlyBuf.writeByteArray(msg.modelData);
    }

    private static SyncPlayerModel decode(ByteBuf buf) {
        FriendlyByteBuf friendlyBuf = new FriendlyByteBuf(buf);
        UUID playerUuid = friendlyBuf.readUUID();
        String modelId = friendlyBuf.readUtf();
        byte[] modelData = friendlyBuf.readByteArray();
        return new SyncPlayerModel(playerUuid, modelId, modelData);
    }

    private final UUID playerUuid;
    private final String modelId;
    private final byte[] modelData;

    public SyncPlayerModel(UUID playerUuid, String modelId, byte[] modelData) {
        this.playerUuid = playerUuid;
        this.modelId = modelId;
        this.modelData = modelData;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public String getModelId() {
        return modelId;
    }

    public byte[] getModelData() {
        return modelData;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(SyncPlayerModel message, IPayloadContext context) {
        context.enqueueWork(() -> {
            YesSteveModel.LOGGER.info("SyncPlayerModel received: player={}, model={}, dataLen={}", 
                    message.playerUuid, message.modelId, message.modelData.length);
            
            try {
                ModelData data = deserializeModelData(message.modelData);
                if (data == null) {
                    YesSteveModel.LOGGER.error("Failed to deserialize model data for {}", message.modelId);
                    return;
                }
                
                // Schedule ALL work on the main thread to avoid race conditions:
                // 1. Register model into GeckoLibCache and MODELS map
                // 2. Apply any pending capability from SyncModelInfo
                // 3. Set player capability
                // This ensures the model is registered BEFORE the capability is set,
                // so the renderer always finds the model in GeckoLibCache.
                final ModelData finalData = data;
                Minecraft.getInstance().execute(() -> {
                    // Step 1: Register the model (geo, textures, animations)
                    ClientModelManager.registerAll(finalData);
                    YesSteveModel.LOGGER.info("Registered synced model: {} with {} textures", 
                            finalData.getModelId(), finalData.getTexture().size());
                    
                    ResourceLocation modelLoc = ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, message.modelId);
                    
                    // Step 2: Apply any pending model info that was deferred because the model wasn't available
                    SyncModelInfo.applyPendingModelInfo(message.playerUuid, modelLoc);
                    SyncModelInfo.applyAllPendingModelInfos();
                    
                    // Step 3: Set the player's capability if the player entity exists
                    Player player = Minecraft.getInstance().level == null ? null : 
                            Minecraft.getInstance().level.getPlayerByUUID(message.playerUuid);
                    if (player != null) {
                        Map<String, byte[]> textureMap = finalData.getTexture();
                        String defaultTexture = finalData.getInfo().getDefaultTexture();
                        String firstTexture;
                        if (defaultTexture != null && textureMap.containsKey(defaultTexture)) {
                            firstTexture = defaultTexture;
                        } else if (textureMap.containsKey("default.png")) {
                            firstTexture = "default.png";
                        } else if (textureMap.containsKey("texture.png")) {
                            firstTexture = "texture.png";
                        } else {
                            firstTexture = textureMap.keySet().stream()
                                    .filter(name -> !name.equals("arrow.png"))
                                    .findFirst()
                                    .orElseGet(() -> textureMap.keySet().iterator().next());
                        }
                        ResourceLocation textureLoc = ModelIdUtil.getSubModelId(modelLoc, firstTexture);
                        player.getData(YSMAttachments.MODEL_INFO).setModelAndTexture(modelLoc, textureLoc);
                        YesSteveModel.LOGGER.info("Set player {} model to {} with texture {}", 
                                player.getName().getString(), modelLoc, textureLoc);
                    }
                });
            } catch (Exception e) {
                YesSteveModel.LOGGER.error("Error in handleClient for SyncPlayerModel", e);
            }
        });
    }
    
    private static ModelData deserializeModelData(byte[] data) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(bais);
            
            String modelId = ois.readUTF();
            boolean isAuth = ois.readBoolean();
            
            Map<String, byte[]> model = readStringMap(ois);
            Map<String, byte[]> texture = readStringMap(ois);
            Map<String, byte[]> animation = readStringMap(ois);
            
            YesSteveModel.LOGGER.info("Deserialized model {}: model={}, texture={}, animation={}", 
                    modelId, model.size(), texture.size(), animation.size());
            
            ModelData modelData = new ModelData(modelId, isAuth, moe.byn.minecraftmod.legacyysm.model.format.Type.UNKNOWN, model, texture, animation);
            
            String defaultTex = ois.readUTF();
            if (!defaultTex.isEmpty()) {
                modelData.getInfo().setDefaultTexture(defaultTex);
            }
            
            return modelData;
        } catch (Exception e) {
            YesSteveModel.LOGGER.error("Failed to deserialize model data", e);
            return null;
        }
    }
    
    private static Map<String, byte[]> readStringMap(ObjectInputStream ois) throws Exception {
        Map<String, byte[]> map = Maps.newHashMap();
        int size = ois.readInt();
        for (int i = 0; i < size; i++) {
            String key = ois.readUTF();
            int len = ois.readInt();
            byte[] value = new byte[len];
            ois.readFully(value);
            map.put(key, value);
        }
        return map;
    }
}
