package moe.byn.minecraftmod.legacyysm.mclib.math.functions.rounding;

import moe.byn.minecraftmod.legacyysm.mclib.math.IValue;
import moe.byn.minecraftmod.legacyysm.mclib.math.functions.Function;
import moe.byn.minecraftmod.legacyysm.util.Keep;

public class Trunc extends Function {
    public Trunc(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    @Keep
    public int getRequiredArguments() {
        return 1;
    }

    @Override
    @Keep
    public double get() {
        double value = this.getArg(0);

        return value < 0 ? Math.ceil(value) : Math.floor(value);
    }
}
