package moe.byn.minecraftmod.legacyysm.model;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import moe.byn.minecraftmod.legacyysm.config.ServerConfig;
import moe.byn.minecraftmod.legacyysm.network.NetworkHandler;
import moe.byn.minecraftmod.legacyysm.network.message.SyncPlayerModel;
import moe.byn.minecraftmod.legacyysm.util.ModelIdValidator;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerModelCache {
    
    private static final Path PLAYER_MODEL_CACHE = ServerModelManager.CACHE.resolve("player_models");
    
    private static final Path PLAYER_MODELS_MAPPING_FILE = PLAYER_MODEL_CACHE.resolve("player_models_mapping.json");
    
    private static final Map<UUID, String> PLAYER_MODELS = Maps.newConcurrentMap();
    
    private static final LinkedHashMap<String, CachedModelInfo> MODEL_CACHE = new LinkedHashMap<>(16, 0.75f, true);
    
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;
        initialized = true;
        
        try {
            Files.createDirectories(PLAYER_MODEL_CACHE);
        } catch (IOException e) {
            YesSteveModel.LOGGER.error("Failed to create player model cache directory", e);
        }
        
        loadPlayerModelsMapping();
        
        YesSteveModel.LOGGER.info("PlayerModelCache initialized with {} player model mappings", PLAYER_MODELS.size());
    }
    
    /**
     * Load UUID -> modelId mapping from disk and restore MODEL_CACHE entries
     * for all mappings whose cache files still exist on disk.
     */
    private static void loadPlayerModelsMapping() {
        File mappingFile = PLAYER_MODELS_MAPPING_FILE.toFile();
        if (!mappingFile.exists()) {
            YesSteveModel.LOGGER.info("No existing player models mapping file found");
            return;
        }
        
        try {
            String json = FileUtils.readFileToString(mappingFile, StandardCharsets.UTF_8);
            Type type = new TypeToken<Map<String, String>>() {}.getType();
            Map<String, String> loaded = GSON.fromJson(json, type);
            
            if (loaded != null) {
                for (Map.Entry<String, String> entry : loaded.entrySet()) {
                    try {
                        UUID uuid = UUID.fromString(entry.getKey());
                        String modelId = entry.getValue();
                        
                        PLAYER_MODELS.put(uuid, modelId);
                        
                        String cacheKey = generateCacheKey(uuid, modelId);
                        File cacheFile = PLAYER_MODEL_CACHE.resolve(cacheKey).toFile();
                        if (cacheFile.exists()) {
                            MODEL_CACHE.put(cacheKey, new CachedModelInfo(uuid, modelId, System.currentTimeMillis()));
                            YesSteveModel.LOGGER.debug("Restored cached model mapping: {} -> {}", uuid, modelId);
                        } else {
                            YesSteveModel.LOGGER.warn("Cache file missing for player {} model {}, removing mapping", uuid, modelId);
                            PLAYER_MODELS.remove(uuid);
                        }
                    } catch (IllegalArgumentException e) {
                        YesSteveModel.LOGGER.warn("Invalid UUID in mapping file: {}", entry.getKey());
                    }
                }
                
                savePlayerModelsMapping();
                YesSteveModel.LOGGER.info("Restored {} player model mappings from disk ({} have cache files)", 
                        loaded.size(), MODEL_CACHE.size());
            }
        } catch (Exception e) {
            YesSteveModel.LOGGER.error("Failed to load player models mapping", e);
        }
    }
    
    /**
     * Persist the current UUID -> modelId mapping to disk.
     */
    private static void savePlayerModelsMapping() {
        try {
            String json = GSON.toJson(PLAYER_MODELS);
            FileUtils.writeStringToFile(PLAYER_MODELS_MAPPING_FILE.toFile(), json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            YesSteveModel.LOGGER.error("Failed to save player models mapping", e);
        }
    }

    public static boolean cachePlayerModel(ServerPlayer player, String modelId, byte[] modelData) {
        init();
        
        if (!ModelIdValidator.isValidModelId(modelId)) {
            YesSteveModel.LOGGER.warn("Player {} tried to cache model with invalid modelId: {}", 
                    player.getName().getString(), modelId);
            return false;
        }
        
        int maxCachedModels = ServerConfig.MAX_CACHED_MODELS.get();
        
        String oldModelId = PLAYER_MODELS.put(player.getUUID(), modelId);
        if (oldModelId != null && !oldModelId.equals(modelId)) {
            removeFromCache(player.getUUID(), oldModelId);
        }
        
        while (MODEL_CACHE.size() >= maxCachedModels) {
            String oldestKey = MODEL_CACHE.keySet().iterator().next();
            CachedModelInfo info = MODEL_CACHE.get(oldestKey);
            if (info != null) {
                removeFromCache(info.ownerUuid, info.modelId);
            }
            YesSteveModel.LOGGER.info("Removed oldest cached model: {} (cache full)", oldestKey);
        }
        
        String cacheKey = generateCacheKey(player.getUUID(), modelId);
        try {
            File cacheFile = PLAYER_MODEL_CACHE.resolve(cacheKey).toFile();
            
            Path canonicalCacheDir = PLAYER_MODEL_CACHE.toRealPath();
            Path canonicalTargetFile = cacheFile.getCanonicalFile().toPath();
            if (!canonicalTargetFile.startsWith(canonicalCacheDir)) {
                YesSteveModel.LOGGER.warn("Path traversal attempt detected from player {} with modelId: {}", 
                        player.getName().getString(), modelId);
                return false;
            }
            
            FileUtils.writeByteArrayToFile(cacheFile, modelData);
            
            MODEL_CACHE.put(cacheKey, new CachedModelInfo(player.getUUID(), modelId, System.currentTimeMillis()));
            
            savePlayerModelsMapping();
            
            YesSteveModel.LOGGER.info("Cached player model: {} (total cached: {})", cacheKey, MODEL_CACHE.size());
            return true;
        } catch (IOException e) {
            YesSteveModel.LOGGER.error("Failed to cache player model", e);
            return false;
        }
    }

    private static void removeFromCache(UUID playerUuid, String modelId) {
        String cacheKey = generateCacheKey(playerUuid, modelId);
        MODEL_CACHE.remove(cacheKey);
        try {
            File cacheFile = PLAYER_MODEL_CACHE.resolve(cacheKey).toFile();
            if (cacheFile.exists()) {
                FileUtils.deleteQuietly(cacheFile);
            }
        } catch (Exception e) {
            YesSteveModel.LOGGER.warn("Failed to delete cached model file: {}", cacheKey);
        }
    }

    public static byte[] getCachedModel(UUID playerUuid, String modelId) {
        init();
        
        String cacheKey = generateCacheKey(playerUuid, modelId);
        CachedModelInfo info = MODEL_CACHE.get(cacheKey);
        if (info != null) {
            try {
                File cacheFile = PLAYER_MODEL_CACHE.resolve(cacheKey).toFile();
                if (cacheFile.exists()) {
                    return FileUtils.readFileToByteArray(cacheFile);
                }
            } catch (IOException e) {
                YesSteveModel.LOGGER.error("Failed to read cached model", e);
            }
            return null;
        }
        
        // Disk fallback for entries evicted from memory but still on disk
        try {
            File cacheFile = PLAYER_MODEL_CACHE.resolve(cacheKey).toFile();
            if (cacheFile.exists()) {
                byte[] data = FileUtils.readFileToByteArray(cacheFile);
                MODEL_CACHE.put(cacheKey, new CachedModelInfo(playerUuid, modelId, System.currentTimeMillis()));
                return data;
            }
        } catch (IOException e) {
            YesSteveModel.LOGGER.error("Failed to read cached model from disk fallback", e);
        }
        
        return null;
    }

    public static String getPlayerModelId(UUID playerUuid) {
        return PLAYER_MODELS.get(playerUuid);
    }

    public static boolean hasCachedModel(UUID playerUuid, String modelId) {
        String cacheKey = generateCacheKey(playerUuid, modelId);
        if (MODEL_CACHE.containsKey(cacheKey)) {
            return true;
        }
        return PLAYER_MODEL_CACHE.resolve(cacheKey).toFile().exists();
    }

    public static void broadcastPlayerModel(ServerPlayer source, String modelId, byte[] modelData) {
        SyncPlayerModel packet = new SyncPlayerModel(source.getUUID(), modelId, modelData);
        NetworkHandler.sendToAllPlayers(packet);
        YesSteveModel.LOGGER.info("Broadcasted model {} from player {} ({} bytes) to all players", 
                modelId, source.getName().getString(), modelData.length);
    }

    public static void sendCachedModelsToPlayer(ServerPlayer target, PlayerList playerList) {
        init();
        
        for (ServerPlayer otherPlayer : playerList.getPlayers()) {
            if (otherPlayer.getUUID().equals(target.getUUID())) {
                continue;
            }
            
            String modelId = PLAYER_MODELS.get(otherPlayer.getUUID());
            if (modelId != null) {
                byte[] modelData = getCachedModel(otherPlayer.getUUID(), modelId);
                if (modelData != null) {
                    SyncPlayerModel packet = new SyncPlayerModel(otherPlayer.getUUID(), modelId, modelData);
                    NetworkHandler.sendToClientPlayer(packet, target);
                    YesSteveModel.LOGGER.debug("Sent cached model {} from player {} to player {}", 
                            modelId, otherPlayer.getName().getString(), target.getName().getString());
                }
            }
        }
    }

    /**
     * Called when a player disconnects. We no longer remove the PLAYER_MODELS mapping
     * so that the model can be restored when the player or other players reconnect.
     * LRU eviction will handle cache size limits.
     */
    public static void onPlayerDisconnect(UUID playerUuid) {
        // Keep the mapping so rejoining players can see cached models.
        // The mapping is persisted to disk and survives server restarts.
        YesSteveModel.LOGGER.debug("Player {} disconnected, keeping model mapping for rejoin", playerUuid);
    }

    private static String generateCacheKey(UUID playerUuid, String modelId) {
        return playerUuid.toString() + "_" + modelId;
    }

    private static class CachedModelInfo {
        final UUID ownerUuid;
        final String modelId;
        final long timestamp;

        CachedModelInfo(UUID ownerUuid, String modelId, long timestamp) {
            this.ownerUuid = ownerUuid;
            this.modelId = modelId;
            this.timestamp = timestamp;
        }
    }
}
