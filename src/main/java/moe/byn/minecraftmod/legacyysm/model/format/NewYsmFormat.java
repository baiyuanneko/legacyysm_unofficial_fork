package moe.byn.minecraftmod.legacyysm.model.format;

import com.ysm.parser.YSMParser;
import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import moe.byn.minecraftmod.legacyysm.data.EncryptTools;
import moe.byn.minecraftmod.legacyysm.data.ModelData;
import moe.byn.minecraftmod.legacyysm.util.Md5Utils;
import moe.byn.minecraftmod.legacyysm.util.NativeLoader;
import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import static moe.byn.minecraftmod.legacyysm.model.ServerModelManager.*;

public final class NewYsmFormat {

    private static final byte[] BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    private static final byte[] YSGP_MAGIC = {0x59, 0x53, 0x47, 0x50};

    private NewYsmFormat() {}

    public static void cacheAllModels(Path rootPath) {
        if (!NativeLoader.load()) {
            return;
        }

        Collection<File> ysmFiles = FileUtils.listFiles(
                rootPath.toFile(), new String[]{"ysm"}, false);
        YesSteveModel.LOGGER.info("NewYsmFormat: Found {} .ysm files in {}",
                ysmFiles.size(), rootPath);

        for (File ysmFile : ysmFiles) {
            String modelId = removeExtension(ysmFile.getName());
            if (ResourceLocation.tryParse(modelId) == null) {
                YesSteveModel.LOGGER.warn("NewYsmFormat: Skipping invalid modelId: {}",
                        modelId);
                continue;
            }
            if (!isV3Format(ysmFile)) {
                continue;
            }

            try {
                YesSteveModel.LOGGER.info("NewYsmFormat: Parsing V3 model: {}", modelId);
                Map<String, byte[]> data = parseToFlatData(ysmFile);
                if (data.isEmpty()) {
                    YesSteveModel.LOGGER.warn("NewYsmFormat: Empty data for {}", modelId);
                    continue;
                }
                if (!data.containsKey(MAIN_MODEL_FILE_NAME)) {
                    YesSteveModel.LOGGER.warn("NewYsmFormat: Missing main.json for {}",
                            modelId);
                    continue;
                }
                if (!data.containsKey(ARM_MODEL_FILE_NAME)) {
                    YesSteveModel.LOGGER.warn("NewYsmFormat: Missing arm.json for {}",
                            modelId);
                    continue;
                }
                if (data.keySet().stream().noneMatch(
                        name -> name.endsWith(".png"))) {
                    YesSteveModel.LOGGER.warn("NewYsmFormat: No texture found for {}",
                            modelId);
                    continue;
                }

                boolean isAuth = rootPath.equals(AUTH);
                ServerModelInfo info = cacheParsedModel(data, modelId, isAuth);
                if (info != null) {
                    CACHE_NAME_INFO.put(modelId, info);
                    if (isAuth) {
                        AUTH_MODELS.add(modelId);
                    }
                    YesSteveModel.LOGGER.info("NewYsmFormat: Cached model: {}", modelId);
                }
            } catch (Exception e) {
                YesSteveModel.LOGGER.error("NewYsmFormat: Failed to cache model {}",
                        modelId, e);
            }
        }
    }

    /**
     * Parse a V3 .ysm through YSMParser JNI, returns the restructured flat
     * file map compatible with the existing FolderFormat pipeline.
     */
    public static Map<String, byte[]> parseToFlatData(File ysmFile)
            throws IOException {
        if (!NativeLoader.load()) {
            throw new IOException(
                    "YSMParser JNI not available on this platform");
        }
        Path tempDir = Files.createTempDirectory("ysm_v3_");
        try {
            boolean ok = YSMParser.parse(
                    ysmFile.getAbsolutePath(), tempDir.toString());
            if (!ok) {
                YesSteveModel.LOGGER.error("NewYsmFormat: YSMParser.parse() failed for {}",
                        ysmFile.getName());
                return Map.of();
            }

            return collectFlatFiles(tempDir);

        } finally {
            FileUtils.deleteDirectory(tempDir.toFile());
        }
    }

    private static Map<String, byte[]> collectFlatFiles(Path parsedDir)
            throws IOException {
        Map<String, byte[]> result = Maps.newHashMap();

        Path nestedModels = parsedDir.resolve("models");
        Path flatModels = parsedDir;
        boolean isNested = Files.isDirectory(nestedModels);
        Path modelsDir = isNested ? nestedModels : flatModels;

        copyModelFile(modelsDir, "main.json", MAIN_MODEL_FILE_NAME, result);
        copyModelFile(modelsDir, "arm.json", ARM_MODEL_FILE_NAME, result);

        Path texturesDir = isNested
                ? parsedDir.resolve("textures") : parsedDir;
        if (Files.isDirectory(texturesDir)) {
            for (File png : FileUtils.listFiles(
                    texturesDir.toFile(), new String[]{"png"}, false)) {
                byte[] data = FileUtils.readFileToByteArray(png);
                result.put(png.getName(), data);
            }
        }

        Path animationsDir = isNested
                ? parsedDir.resolve("animations") : parsedDir;
        if (Files.isDirectory(animationsDir)) {
            for (File anim : FileUtils.listFiles(
                    animationsDir.toFile(), new String[]{"json"}, false)) {
                byte[] data = FileUtils.readFileToByteArray(anim);
                String name = anim.getName();

                if (name.equals("main.animation.json")) {
                    result.put(MAIN_ANIMATION_FILE_NAME, data);
                } else if (name.equals("arm.animation.json")) {
                    result.put(ARM_ANIMATION_FILE_NAME, data);
                } else if (name.equals("extra.animation.json")) {
                    result.put(EXTRA_ANIMATION_FILE_NAME, data);
                }
            }
        }

        return result;
    }

    private static void copyModelFile(Path modelsDir, String srcName,
                                       String dstName, Map<String, byte[]> result)
            throws IOException {
        Path srcPath = modelsDir.resolve(srcName);
        if (!Files.isRegularFile(srcPath)) {
            return;
        }
        byte[] rawJson = FileUtils.readFileToByteArray(srcPath.toFile());
        result.put(dstName, rawJson);
    }

    private static ServerModelInfo cacheParsedModel(
            Map<String, byte[]> data, String modelId, boolean isAuth) {
        try {
            ModelData modelData = YsmFormat.getModelData(data, modelId, isAuth);

            byte[] dataBytes = EncryptTools.assembleEncryptModels(modelData);
            modelData.setMd5(Md5Utils.md5Hex(dataBytes)
                    .toUpperCase(Locale.US));
            FileUtils.writeByteArrayToFile(
                    CACHE_SERVER.resolve(modelData.getInfo().getMd5())
                            .toFile(), dataBytes);
            return modelData.getInfo();
        } catch (Exception e) {
            YesSteveModel.LOGGER.error("NewYsmFormat: cacheParsedModel failed", e);
            return null;
        }
    }

    public static boolean isV3Format(File ysmFile) {
        try (InputStream in = Files.newInputStream(ysmFile.toPath())) {
            byte[] header = IOUtils.toByteArray(in, 8);
            if (header.length < 8) {
                return false;
            }

            if (header[0] == BOM[0] && header[1] == BOM[1]
                    && header[2] == BOM[2]
                    && header[3] == YSGP_MAGIC[0]
                    && header[4] == YSGP_MAGIC[1]
                    && header[5] == YSGP_MAGIC[2]
                    && header[6] == YSGP_MAGIC[3]) {
                return true;
            }

            return false;
        } catch (IOException e) {
            return false;
        }
    }
}
