package moe.byn.minecraftmod.legacyysm.geckolib3.core.molang.functions;

import moe.byn.minecraftmod.legacyysm.mclib.math.IValue;
import moe.byn.minecraftmod.legacyysm.mclib.math.functions.Function;
import moe.byn.minecraftmod.legacyysm.util.Keep;

public class CosDegrees extends Function {
    public CosDegrees(IValue[] values, String name) throws Exception {
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
        return Math.cos(this.getArg(0) / 180 * Math.PI);
    }
}
