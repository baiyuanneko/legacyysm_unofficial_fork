package moe.byn.minecraftmod.legacyysm.mclib.math.functions.classic;

import moe.byn.minecraftmod.legacyysm.mclib.math.IValue;
import moe.byn.minecraftmod.legacyysm.mclib.math.functions.Function;
import moe.byn.minecraftmod.legacyysm.util.Keep;

public class Mod extends Function {
    public Mod(IValue[] values, String name) throws Exception {
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
        return this.getArg(0) % this.getArg(1);
    }
}
