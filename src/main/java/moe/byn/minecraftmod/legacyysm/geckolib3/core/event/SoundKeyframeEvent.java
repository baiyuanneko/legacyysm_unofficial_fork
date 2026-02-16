/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package moe.byn.minecraftmod.legacyysm.geckolib3.core.event;

import moe.byn.minecraftmod.legacyysm.geckolib3.core.controller.AnimationController;

public class SoundKeyframeEvent<T> extends KeyframeEvent<T> {
    public final String sound;

    @SuppressWarnings("rawtypes")
    public SoundKeyframeEvent(T entity, double animationTick, String sound, AnimationController controller) {
        super(entity, animationTick, controller);
        this.sound = sound;
    }
}
