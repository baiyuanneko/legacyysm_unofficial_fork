package moe.byn.minecraftmod.legacyysm.bukkit.message;

import moe.byn.minecraftmod.legacyysm.bukkit.client.NPCData;
import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Map;
import java.util.UUID;

public class SyncNpcDataMessage implements CustomPacketPayload {
    public static final Type<SyncNpcDataMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("legacyysm_byn", "sync_npc_data"));
    
    public static final StreamCodec<ByteBuf, SyncNpcDataMessage> STREAM_CODEC = StreamCodec.of(
            SyncNpcDataMessage::encode,
            SyncNpcDataMessage::decode
    );
    
    private final Map<UUID, Pair<ResourceLocation, ResourceLocation>> data;

    public SyncNpcDataMessage(Map<UUID, Pair<ResourceLocation, ResourceLocation>> data) {
        this.data = data;
    }

    private static void encode(ByteBuf buf, SyncNpcDataMessage message) {
    }

    private static SyncNpcDataMessage decode(ByteBuf buf) {
        FriendlyByteBuf friendlyBuf = new FriendlyByteBuf(buf);
        Map<UUID, Pair<ResourceLocation, ResourceLocation>> map = Maps.newHashMap();
        int size = friendlyBuf.readInt();
        for (int i = 0; i < size; i++) {
            UUID uuid = friendlyBuf.readUUID();
            ResourceLocation modelId = ResourceLocation.parse(friendlyBuf.readUtf());
            ResourceLocation textureId = ResourceLocation.parse(friendlyBuf.readUtf());
            map.put(uuid, Pair.of(modelId, textureId));
        }
        return new SyncNpcDataMessage(map);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(SyncNpcDataMessage message, IPayloadContext context) {
        context.enqueueWork(() -> handleMessage(message));
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleMessage(SyncNpcDataMessage message) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer != null) {
            NPCData.addAll(message.data);
        }
    }
}
