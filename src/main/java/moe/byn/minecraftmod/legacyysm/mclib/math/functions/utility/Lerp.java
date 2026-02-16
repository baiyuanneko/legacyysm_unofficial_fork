package moe.byn.minecraftmod.legacyysm.mclib.math.functions.utility;

import moe.byn.minecraftmod.legacyysm.mclib.math.IValue;
import moe.byn.minecraftmod.legacyysm.mclib.math.functions.Function;
import moe.byn.minecraftmod.legacyysm.mclib.utils.Interpolations;
import moe.byn.minecraftmod.legacyysm.util.Keep;

public class Lerp extends Function {
    public Lerp(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    @Keep
    public int getRequiredArguments() {
        return 3;
    }

    @Override
    @Keep
    public double get() {
        return Interpolations.lerp(this.getArg(0), this.getArg(1), this.getArg(2));
    }
}
