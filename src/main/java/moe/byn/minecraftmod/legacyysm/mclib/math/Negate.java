package moe.byn.minecraftmod.legacyysm.mclib.math;

import moe.byn.minecraftmod.legacyysm.util.Keep;

public class Negate implements IValue {
    public IValue value;

    public Negate(IValue value) {
        this.value = value;
    }

    @Override
    @Keep
    public double get() {
        return this.value.get() == 0 ? 1 : 0;
    }

    @Override
    @Keep
    public String toString() {
        return "!" + this.value.toString();
    }
}
