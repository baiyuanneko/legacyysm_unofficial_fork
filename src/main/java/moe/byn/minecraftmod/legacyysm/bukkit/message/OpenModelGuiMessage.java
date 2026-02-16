package moe.byn.minecraftmod.legacyysm.bukkit.message;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
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

    private static void handleMessage(OpenModelGuiMessage message) {
        try {
            Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
            java.lang.reflect.Method getInstanceMethod = minecraftClass.getMethod("getInstance");
            Object mc = getInstanceMethod.invoke(null);
            
            java.lang.reflect.Field playerField = minecraftClass.getDeclaredField("player");
            Object localPlayer = playerField.get(mc);
            
            if (localPlayer != null) {
                Class<?> localPlayerClass = localPlayer.getClass();
                java.lang.reflect.Method levelMethod = localPlayerClass.getMethod("level");
                Object level = levelMethod.invoke(localPlayer);
                
                Class<?> levelClass = level.getClass();
                java.lang.reflect.Method getEntityMethod = levelClass.getMethod("getEntity", int.class);
                Entity entity = (Entity) getEntityMethod.invoke(level, message.entityId);
                
                if (entity instanceof Player player) {
                    CURRENT_NPC_ID = message.npcId;
                    
                    Class<?> playerModelScreenClass = Class.forName("moe.byn.minecraftmod.legacyysm.client.gui.PlayerModelScreen");
                    java.lang.reflect.Constructor<?> constructor = playerModelScreenClass.getConstructor(Player.class);
                    Object screen = constructor.newInstance(player);
                    
                    java.lang.reflect.Method setScreenMethod = minecraftClass.getMethod("setScreen", 
                        Class.forName("net.minecraft.client.gui.screens.Screen"));
                    setScreenMethod.invoke(mc, screen);
                }
            }
        } catch (Exception e) {
            YesSteveModel.LOGGER.error("Failed to handle OpenModelGuiMessage", e);
        }
    }
}
