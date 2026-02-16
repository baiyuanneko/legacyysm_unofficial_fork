package moe.byn.minecraftmod.legacyysm.geckolib3.geo.render;

import moe.byn.minecraftmod.legacyysm.geckolib3.geo.raw.pojo.ModelProperties;
import moe.byn.minecraftmod.legacyysm.geckolib3.geo.raw.tree.RawBoneGroup;
import moe.byn.minecraftmod.legacyysm.geckolib3.geo.raw.tree.RawGeometryTree;
import moe.byn.minecraftmod.legacyysm.geckolib3.geo.render.built.GeoBone;
import moe.byn.minecraftmod.legacyysm.geckolib3.geo.render.built.GeoModel;
import moe.byn.minecraftmod.legacyysm.util.Keep;

public interface IGeoBuilder {
    @Keep
    GeoModel constructGeoModel(RawGeometryTree geometryTree);

    @Keep
    GeoBone constructBone(RawBoneGroup bone, ModelProperties properties, GeoBone parent);
}
