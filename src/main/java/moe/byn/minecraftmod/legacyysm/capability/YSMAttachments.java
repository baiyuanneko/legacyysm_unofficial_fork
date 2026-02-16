package moe.byn.minecraftmod.legacyysm.capability;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class YSMAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = 
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, YesSteveModel.MOD_ID);

    public static final Supplier<AttachmentType<ModelInfoCapability>> MODEL_INFO = 
        ATTACHMENTS.register("model_info", () -> 
            AttachmentType.serializable(ModelInfoCapability::new).build());

    public static final Supplier<AttachmentType<AuthModelsCapability>> AUTH_MODELS = 
        ATTACHMENTS.register("auth_models", () -> 
            AttachmentType.serializable(AuthModelsCapability::new).build());

    public static final Supplier<AttachmentType<StarModelsCapability>> STAR_MODELS = 
        ATTACHMENTS.register("star_models", () -> 
            AttachmentType.serializable(StarModelsCapability::new).build());
}
