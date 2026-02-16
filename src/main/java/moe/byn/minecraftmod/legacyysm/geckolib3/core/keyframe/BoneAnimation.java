/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package moe.byn.minecraftmod.legacyysm.geckolib3.core.keyframe;

import moe.byn.minecraftmod.legacyysm.mclib.math.IValue;

public class BoneAnimation {
    public final String boneName;
    public VectorKeyFrameList<KeyFrame<IValue>> rotationKeyFrames;
    public VectorKeyFrameList<KeyFrame<IValue>> positionKeyFrames;
    public VectorKeyFrameList<KeyFrame<IValue>> scaleKeyFrames;

    public BoneAnimation(String boneName) {
        this.boneName = boneName;
    }
}