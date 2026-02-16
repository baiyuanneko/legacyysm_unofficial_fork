package moe.byn.minecraftmod.legacyysm.network.message;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class CompleteFeedback implements CustomPacketPayload {
    public static final CompleteFeedback INSTANCE = new CompleteFeedback();
    
    public static final CustomPacketPayload.Type<CompleteFeedback> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "complete_feedback"));

    public static final StreamCodec<ByteBuf, CompleteFeedback> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    public CompleteFeedback() {
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(CompleteFeedback message, IPayloadContext context) {
        context.enqueueWork(() -> {
            try {
                Class<?> uploadManagerClass = Class.forName("moe.byn.minecraftmod.legacyysm.client.upload.UploadManager");
                java.lang.reflect.Method finishUploadMethod = uploadManagerClass.getMethod("finishUpload");
                finishUploadMethod.invoke(null);
            } catch (Exception e) {
                YesSteveModel.LOGGER.error("Failed to finish upload", e);
            }
        });
    }
}
