package moe.byn.minecraftmod.legacyysm.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 服务端专用配置
 * 这些配置只会在服务端生效，客户端会忽略
 */
public class ServerConfig {
    public static ModConfigSpec CONFIG_SPEC;
    
    /**
     * 是否允许模型互相同步
     * 开启后，客户端可以选择本地模型，服务端会缓存并分发给其他玩家
     */
    public static ModConfigSpec.BooleanValue ALLOW_MODEL_SYNC;
    
    /**
     * 服务端最多缓存的玩家模型数量
     */
    public static ModConfigSpec.IntValue MAX_CACHED_MODELS;
    
    /**
     * 每个玩家最多缓存的模型数量
     */
    public static ModConfigSpec.IntValue MAX_MODELS_PER_PLAYER;
    
    /**
     * 单个模型最大大小（字节），默认50MB
     */
    public static ModConfigSpec.IntValue MAX_MODEL_SIZE_BYTES;
    
    public static ModConfigSpec init() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        
        builder.push("model_sync");
        
        builder.comment("Allow client models to sync between players.",
                "When enabled, clients can select their local models,",
                "and the server will cache and distribute them to other players.",
                "When disabled, only server-side models are available (original behavior).");
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
