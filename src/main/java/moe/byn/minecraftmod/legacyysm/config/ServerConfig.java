package moe.byn.minecraftmod.legacyysm.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfig {
    public static ModConfigSpec CONFIG_SPEC;
    
    public static ModConfigSpec.BooleanValue ALLOW_MODEL_SYNC;
    public static ModConfigSpec.IntValue MAX_CACHED_MODELS;
    public static ModConfigSpec.IntValue MAX_MODELS_PER_PLAYER;
    public static ModConfigSpec.IntValue MAX_MODEL_SIZE_BYTES;
    
    public static ModConfigSpec init() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        
        builder.push("model_sync");
        
        builder.comment("Allow clients to upload and sync their local models to the server.",
                "When true: clients can upload models, server caches and distributes them.",
                "When false (default): only server-to-client model distribution is allowed, clients cannot upload models.");
        ALLOW_MODEL_SYNC = builder.define("allowModelSync", false);
        
        builder.comment("Maximum number of different player models the server will cache.",
                "When this limit is reached, the oldest cached model will be removed.");
        MAX_CACHED_MODELS = builder.defineInRange("maxCachedModels", 20, 1, 100);
        
        builder.comment("Maximum number of models each player can have cached on the server.",
                "When a player uploads a new model, their old cached model is removed.");
        MAX_MODELS_PER_PLAYER = builder.defineInRange("maxModelsPerPlayer", 1, 1, 10);
        
        builder.comment("Maximum size of a single model in bytes.",
                "Default: 52428800 (50MB). Models larger than this will be rejected.");
        MAX_MODEL_SIZE_BYTES = builder.defineInRange("maxModelSizeBytes", 52428800, 1048576, 104857600);
        
        builder.pop();
        
        CONFIG_SPEC = builder.build();
        return CONFIG_SPEC;
    }
}
