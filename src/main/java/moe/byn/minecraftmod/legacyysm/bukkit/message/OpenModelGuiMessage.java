package moe.byn.minecraftmod.legacyysm.bukkit.message;

import moe.byn.minecraftmod.legacyysm.client.gui.PlayerModelScreen;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class OpenModelGuiMessage implements CustomPacketPayload {
    public static final Type<OpenModelGuiMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("legacyysm_byn", "open_model_gui"));
    
    public static final StreamCodec<ByteBuf, OpenModelGuiMessage> STREAM_CODEC = StreamCodec.of(
            OpenModelGuiMessage::encode,
            OpenModelGuiMessage::decode
    );
    
    public static int CURRENT_NPC_ID = -1;
    private final int entityId;
    private final int npcId;

    public OpenModelGuiMessage(int entityId, int npcId) {
        this.entityId = entityId;
        this.npcId = npcId;
    }

    private static void encode(ByteBuf buf, OpenModelGuiMessage message) {
        buf.writeInt(message.entityId);
        buf.writeInt(message.npcId);
    }

    private static OpenModelGuiMessage decode(ByteBuf buf) {
        return new OpenModelGuiMessage(buf.readInt(), buf.readInt());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(OpenModelGuiMessage message, IPayloadContext context) {
        context.enqueueWork(() -> handleMessage(message));
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleMessage(OpenModelGuiMessage message) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.entityId);
            if (entity instanceof Player player) {
                CURRENT_NPC_ID = message.npcId;
                Minecraft.getInstance().setScreen(new PlayerModelScreen(player));
            }
        }
    }
}
