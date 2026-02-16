package moe.byn.minecraftmod.legacyysm.mclib.math.functions.limit;

import moe.byn.minecraftmod.legacyysm.mclib.math.IValue;
import moe.byn.minecraftmod.legacyysm.mclib.math.functions.Function;
import moe.byn.minecraftmod.legacyysm.util.Keep;

public class Max extends Function {
    public Max(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    @Keep
    public int getRequiredArguments() {
        return 2;
    }

    @Override
    @Keep
    public double get() {
        return Math.max(this.getArg(0), this.getArg(1));
    }
}
