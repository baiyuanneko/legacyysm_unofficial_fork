package moe.byn.minecraftmod.legacyysm.bukkit.message;

import moe.byn.minecraftmod.legacyysm.bukkit.client.NPCData;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public class UpdateNpcDataMessage implements CustomPacketPayload {
    public static final Type<UpdateNpcDataMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("legacyysm_byn", "update_npc_data"));
    
    public static final StreamCodec<ByteBuf, UpdateNpcDataMessage> STREAM_CODEC = StreamCodec.of(
            UpdateNpcDataMessage::encode,
            UpdateNpcDataMessage::decode
    );
    
    private final UUID uuid;
    private final ResourceLocation modelId;
    private final ResourceLocation textureId;

    public UpdateNpcDataMessage(UUID uuid, ResourceLocation modelId, ResourceLocation textureId) {
        this.uuid = uuid;
        this.modelId = modelId;
        this.textureId = textureId;
    }

    private static void encode(ByteBuf buf, UpdateNpcDataMessage message) {
    }

    private static UpdateNpcDataMessage decode(ByteBuf buf) {
        FriendlyByteBuf friendlyBuf = new FriendlyByteBuf(buf);
        UUID uuid = friendlyBuf.readUUID();
        ResourceLocation modelId = ResourceLocation.parse(friendlyBuf.readUtf());
        ResourceLocation textureId = ResourceLocation.parse(friendlyBuf.readUtf());
        return new UpdateNpcDataMessage(uuid, modelId, textureId);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(UpdateNpcDataMessage message, IPayloadContext context) {
        context.enqueueWork(() -> handleMessage(message));
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleMessage(UpdateNpcDataMessage message) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer != null) {
            NPCData.put(message.uuid, message.modelId, message.textureId);
        }
    }
}
