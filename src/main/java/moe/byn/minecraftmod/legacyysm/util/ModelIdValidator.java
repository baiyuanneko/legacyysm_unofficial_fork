package moe.byn.minecraftmod.legacyysm.util;

import net.minecraft.resources.ResourceLocation;

public final class ModelIdValidator {
    
    private static final int MAX_MODEL_ID_LENGTH = 64;
    
    private ModelIdValidator() {}
    
    public static boolean isValidModelId(String modelId) {
        if (modelId == null || modelId.isEmpty()) {
            return false;
        }
        
        if (modelId.length() > MAX_MODEL_ID_LENGTH) {
            return false;
        }
        
        if (modelId.contains("..")) {
            return false;
        }
        
        if (modelId.contains("/") || modelId.contains("\\")) {
            return false;
        }
        
        if (modelId.contains("\0")) {
            return false;
        }
        
        if (!ResourceLocation.isValidNamespace(modelId) && !isValidPath(modelId)) {
            return false;
        }
        
        for (char c : modelId.toCharArray()) {
            if (!isValidModelIdChar(c)) {
                return false;
            }
        }
        
        return true;
    }
    
    private static boolean isValidModelIdChar(char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
               (c >= '0' && c <= '9') ||
               c == '_' || c == '-' || c == '.';
    }
    
    private static boolean isValidPath(String path) {
        for (int i = 0; i < path.length(); ++i) {
            if (!ResourceLocation.validPathChar(path.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    
    public static String sanitizeModelId(String modelId) {
        if (modelId == null) {
            return null;
        }
        
        String sanitized = modelId
                .replace("/", "_")
                .replace("\\", "_")
                .replace("..", "_")
                .replace("\0", "");
        
        StringBuilder sb = new StringBuilder();
        for (char c : sanitized.toCharArray()) {
            if (isValidModelIdChar(c)) {
                sb.append(c);
            }
        }
        
        String result = sb.toString();
        if (result.length() > MAX_MODEL_ID_LENGTH) {
            result = result.substring(0, MAX_MODEL_ID_LENGTH);
        }
        
        return result;
    }
}
