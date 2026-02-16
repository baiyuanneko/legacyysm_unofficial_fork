package moe.byn.minecraftmod.legacyysm.mclib.math.functions.classic;

import moe.byn.minecraftmod.legacyysm.mclib.math.IValue;
import moe.byn.minecraftmod.legacyysm.mclib.math.functions.Function;
import moe.byn.minecraftmod.legacyysm.util.Keep;

public class Pi extends Function {
    public Pi(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    @Keep
    public double get() {
        return 3.141592653589793d;
    }
}
