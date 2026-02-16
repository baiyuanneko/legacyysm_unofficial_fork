/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */
package moe.byn.minecraftmod.legacyysm.geckolib3.core;

import moe.byn.minecraftmod.legacyysm.geckolib3.core.manager.AnimationData;
import moe.byn.minecraftmod.legacyysm.geckolib3.core.manager.AnimationFactory;
import moe.byn.minecraftmod.legacyysm.util.Keep;

/**
 * 任何想要附加动画的模型，都需要继承此接口
 */
public interface IAnimatable {
    /**
     * 注册动画控制器
     *
     * @param data 数据
     */
    @Keep
    void registerControllers(AnimationData data);

    /**
     * 动画实例构造
     *
     * @return AnimationFactory
     */
    @Keep
    AnimationFactory getFactory();
}
