package moe.byn.minecraftmod.legacyysm.util;

import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Locale;

public final class ModelIdUtil {
    public static ResourceLocation getSubModelId(ResourceLocation id, String subName) {
        String sanitized = subName.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9/._-]", "_");
        if (!sanitized.equals(subName.toLowerCase(Locale.ROOT))) {
            int dotIdx = sanitized.lastIndexOf('.');
            String hash = Integer.toHexString(subName.hashCode());
            if (dotIdx > 0) {
                sanitized = sanitized.substring(0, dotIdx) + "_" + hash + sanitized.substring(dotIdx);
            } else {
                sanitized = sanitized + "_" + hash;
            }
        }
        String newPath = id.getPath() + "/" + sanitized;
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), newPath);
    }

    public static ResourceLocation getMainId(ResourceLocation id) {
        return getSubModelId(id, "main");
    }

    public static ResourceLocation getArmId(ResourceLocation id) {
        return getSubModelId(id, "arm");
    }

    public static ResourceLocation getModelIdFromMainId(ResourceLocation mainId) {
        String newPath = mainId.getPath().substring(0, mainId.getPath().length() - 5);
        return ResourceLocation.fromNamespaceAndPath(mainId.getNamespace(), newPath);
    }

    @Nullable
    public static String getSubNameFromId(ResourceLocation mainId) {
        String[] split = mainId.getPath().split("/", 2);
        if (split.length == 2) {
            return split[1];
        }
        return StringUtils.EMPTY;
    }
}
