package moe.byn.minecraftmod.legacyysm.geckolib3.resource;

import moe.byn.minecraftmod.legacyysm.geckolib3.core.molang.MolangParser;
import moe.byn.minecraftmod.legacyysm.geckolib3.file.AnimationFile;
import moe.byn.minecraftmod.legacyysm.geckolib3.geo.render.built.GeoModel;
import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class GeckoLibCache {
    private static GeckoLibCache INSTANCE;
    public final MolangParser parser = new MolangParser();
    private final Map<ResourceLocation, AnimationFile> animations = Maps.newHashMap();
    private final Map<ResourceLocation, GeoModel> geoModels = Maps.newHashMap();

    public static GeckoLibCache getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GeckoLibCache();
            return INSTANCE;
        }
        return INSTANCE;
    }

    public Map<ResourceLocation, AnimationFile> getAnimations() {
        return animations;
    }

    public Map<ResourceLocation, GeoModel> getGeoModels() {
        return geoModels;
    }
}
