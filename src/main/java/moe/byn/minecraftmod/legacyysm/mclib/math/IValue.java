package moe.byn.minecraftmod.legacyysm.mclib.math;

import moe.byn.minecraftmod.legacyysm.util.Keep;

public interface IValue {
    /**
     * 获取计算值或存储值
     */
    @Keep
    double get();
}
