package moe.byn.minecraftmod.legacyysm.mclib.math.functions.classic;

import moe.byn.minecraftmod.legacyysm.mclib.math.IValue;
import moe.byn.minecraftmod.legacyysm.mclib.math.functions.Function;
import moe.byn.minecraftmod.legacyysm.util.Keep;

public class Pow extends Function {
    public Pow(IValue[] values, String name) throws Exception {
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
        return Math.pow(this.getArg(0), this.getArg(1));
    }
}
