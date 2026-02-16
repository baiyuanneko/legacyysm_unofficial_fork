package moe.byn.minecraftmod.legacyysm.mclib.math;

import moe.byn.minecraftmod.legacyysm.util.Keep;

public class Negative implements IValue {
    public IValue value;

    public Negative(IValue value) {
        this.value = value;
    }

    @Override
    @Keep
    public double get() {
        return -this.value.get();
    }

    @Override
    @Keep
    public String toString() {
        return "-" + this.value.toString();
    }
}
