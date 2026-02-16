package moe.byn.minecraftmod.legacyysm.bukkit.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SetNpcModelAndTexture implements CustomPacketPayload {
    public static final Type<SetNpcModelAndTexture> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("legacyysm_byn", "set_npc_model_and_texture"));
    
    public static final StreamCodec<ByteBuf, SetNpcModelAndTexture> STREAM_CODEC = StreamCodec.of(
            SetNpcModelAndTexture::encode,
            SetNpcModelAndTexture::decode
    );
    
    private final ResourceLocation modelId;
    private final ResourceLocation selectTexture;
    private final int npcId;

    public SetNpcModelAndTexture(ResourceLocation modelId, ResourceLocation selectTexture, int npcId) {
        this.modelId = modelId;
        this.selectTexture = selectTexture;
        this.npcId = npcId;
    }

    private static void encode(ByteBuf buf, SetNpcModelAndTexture message) {
        FriendlyByteBuf friendlyBuf = new FriendlyByteBuf(buf);
        friendlyBuf.writeResourceLocation(message.modelId);
        friendlyBuf.writeResourceLocation(message.selectTexture);
        friendlyBuf.writeVarInt(message.npcId);
    }

    private static SetNpcModelAndTexture decode(ByteBuf buf) {
        FriendlyByteBuf friendlyBuf = new FriendlyByteBuf(buf);
        return new SetNpcModelAndTexture(friendlyBuf.readResourceLocation(), friendlyBuf.readResourceLocation(), friendlyBuf.readVarInt());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(SetNpcModelAndTexture message, IPayloadContext context) {
        // Empty handler - server-side only, no additional processing needed
    }
}