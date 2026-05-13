package moe.byn.minecraftmod.legacyysm.model.format;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import moe.byn.minecraftmod.legacyysm.data.EncryptTools;
import moe.byn.minecraftmod.legacyysm.data.ModelData;
import moe.byn.minecraftmod.legacyysm.geckolib3.geo.raw.pojo.Converter;
import moe.byn.minecraftmod.legacyysm.geckolib3.geo.raw.pojo.RawGeoModel;
import moe.byn.minecraftmod.legacyysm.util.Md5Utils;
import moe.byn.minecraftmod.legacyysm.util.ObjectStreamUtil;
import moe.byn.minecraftmod.legacyysm.util.YesModelUtils;
import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import static moe.byn.minecraftmod.legacyysm.model.ServerModelManager.*;

public final class YsmFormat {
    public static void cacheAllModels(Path rootPath) {
        Collection<File> ysmFiles = FileUtils.listFiles(rootPath.toFile(), new String[]{"ysm"}, false);
        YesSteveModel.LOGGER.info("YsmFormat: Found {} .ysm files in {}", ysmFiles.size(), rootPath);
        for (File ysmFile : ysmFiles) {
            String modelId = removeExtension(ysmFile.getName());
            if (ResourceLocation.tryParse(modelId) == null) {
                YesSteveModel.LOGGER.warn("YsmFormat: Skipping invalid modelId: {}", modelId);
                continue;
            }
            try {
                Map<String, byte[]> data = YesModelUtils.input(ysmFile);
                if (data.isEmpty()) {
                    YesSteveModel.LOGGER.warn("YsmFormat: Empty data for {}", modelId);
                    continue;
                }
                if (!data.containsKey(MAIN_MODEL_FILE_NAME)) {
                    YesSteveModel.LOGGER.warn("YsmFormat: Missing main.json for {}", modelId);
                    continue;
                }
                if (!data.containsKey(ARM_MODEL_FILE_NAME)) {
                    YesSteveModel.LOGGER.warn("YsmFormat: Missing arm.json for {}", modelId);
                    continue;
                }
                if (data.keySet().stream().noneMatch(fileName -> fileName.endsWith(".png"))) {
                    YesSteveModel.LOGGER.warn("YsmFormat: No texture found for {}", modelId);
                    continue;
                }

                if (rootPath.equals(AUTH)) {
                    ServerModelInfo info = cacheModel(data, modelId, true);
                    if (info != null) {
                        CACHE_NAME_INFO.put(modelId, info);
                        AUTH_MODELS.add(modelId);
                        YesSteveModel.LOGGER.info("YsmFormat: Cached auth model: {}", modelId);
                    }
                } else {
                    ServerModelInfo info = cacheModel(data, modelId, false);
                    if (info != null) {
                        CACHE_NAME_INFO.put(modelId, info);
                        YesSteveModel.LOGGER.info("YsmFormat: Cached model: {}", modelId);
                    }
                }
            } catch (IOException e) {
                YesSteveModel.LOGGER.error("YsmFormat: Failed to cache model {}", modelId, e);
            }
        }
    }

    private static ServerModelInfo cacheModel(Map<String, byte[]> input, String modelId, boolean isAuth) {
        try {
            ModelData data = getModelData(input, modelId, isAuth);
            byte[] dataBytes = EncryptTools.assembleEncryptModels(data);
            data.setMd5(Md5Utils.md5Hex(dataBytes).toUpperCase(Locale.US));
            FileUtils.writeByteArrayToFile(CACHE_SERVER.resolve(data.getInfo().getMd5()).toFile(), dataBytes);
            return data.getInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @NotNull
    public static ModelData getModelData(Map<String, byte[]> data, String modelId, boolean isAuth) throws IOException {
        Map<String, byte[]> model = Maps.newHashMap();
        model.put("main", getBytes(data, MAIN_MODEL_FILE_NAME));
        model.put("arm", getBytes(data, ARM_MODEL_FILE_NAME));

        Map<String, byte[]> texture = Maps.newLinkedHashMap();
        data.forEach((name, textureData) -> {
            if (name.endsWith(".png")) {
                texture.put(name, textureData);
            }
        });

        Map<String, byte[]> animation = Maps.newHashMap();
        animation.put("main", getBytes(data, MAIN_ANIMATION_FILE_NAME));
        animation.put("arm", getBytes(data, ARM_ANIMATION_FILE_NAME));
        animation.put("extra", getBytes(data, EXTRA_ANIMATION_FILE_NAME));

        ModelData modelData = new ModelData(modelId, isAuth, Type.YSM, model, texture, animation);

        if (data.containsKey("ysm.json")) {
            try {
                String ysmJsonStr = new String(data.get("ysm.json"), StandardCharsets.UTF_8);
                com.google.gson.JsonObject ysmRoot = YesSteveModel.GSON.fromJson(ysmJsonStr, com.google.gson.JsonObject.class);
                if (ysmRoot != null && ysmRoot.has("properties")) {
                    String defaultTex = ysmRoot.getAsJsonObject("properties").get("default_texture").getAsString();
                    if (defaultTex != null && !defaultTex.isEmpty()) {
                        modelData.getInfo().setDefaultTexture(defaultTex + ".png");
                    }
                }
            } catch (Exception e) {
                YesSteveModel.LOGGER.warn("YsmFormat: Failed to parse default_texture from ysm.json for {}", modelId, e);
            }
        }

        return modelData;
    }

    private static byte[] getBytes(Map<String, byte[]> data, String fileName) throws IOException {
        if (MAIN_ANIMATION_FILE_NAME.equals(fileName) && !data.containsKey(MAIN_ANIMATION_FILE_NAME)) {
            Path filePath = CUSTOM.resolve("default/main.animation.json");
            return FileUtils.readFileToByteArray(filePath.toFile());
        }
        if (ARM_ANIMATION_FILE_NAME.equals(fileName) && !data.containsKey(ARM_ANIMATION_FILE_NAME)) {
            Path filePath = CUSTOM.resolve("default/arm.animation.json");
            return FileUtils.readFileToByteArray(filePath.toFile());
        }
        if (EXTRA_ANIMATION_FILE_NAME.equals(fileName) && !data.containsKey(EXTRA_ANIMATION_FILE_NAME)) {
            Path filePath = CUSTOM.resolve("default/extra.animation.json");
            return FileUtils.readFileToByteArray(filePath.toFile());
        }

        if (MAIN_MODEL_FILE_NAME.equals(fileName) || ARM_MODEL_FILE_NAME.equals(fileName)) {
            String modelJson = new String(data.get(fileName), StandardCharsets.UTF_8);
            RawGeoModel rawModel = Converter.fromJsonString(modelJson);
            return ObjectStreamUtil.toByteArray(rawModel);
        }

        return data.get(fileName);
    }
}
