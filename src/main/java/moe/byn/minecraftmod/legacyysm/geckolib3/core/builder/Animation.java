/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */
package moe.byn.minecraftmod.legacyysm.geckolib3.core.builder;

import moe.byn.minecraftmod.legacyysm.geckolib3.core.builder.ILoopType.EDefaultLoopTypes;
import moe.byn.minecraftmod.legacyysm.geckolib3.core.keyframe.BoneAnimation;
import moe.byn.minecraftmod.legacyysm.geckolib3.core.keyframe.EventKeyFrame;
import moe.byn.minecraftmod.legacyysm.geckolib3.core.keyframe.ParticleEventKeyFrame;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

public class Animation {
    public String animationName;
    public double animationLength = -1;
    public ILoopType loop = EDefaultLoopTypes.LOOP;
    public List<BoneAnimation> boneAnimations;
    public List<EventKeyFrame<String>> soundKeyFrames = new ObjectArrayList<>();
    public List<ParticleEventKeyFrame> particleKeyFrames = new ObjectArrayList<>();
    public List<EventKeyFrame<String>> customInstructionKeyframes = new ObjectArrayList<>();
}
