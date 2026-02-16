package moe.byn.minecraftmod.legacyysm.mclib.math.functions.limit;

import moe.byn.minecraftmod.legacyysm.mclib.math.IValue;
import moe.byn.minecraftmod.legacyysm.mclib.math.functions.Function;
import moe.byn.minecraftmod.legacyysm.util.Keep;
import net.minecraft.util.Mth;

public class Clamp extends Function {
    public Clamp(IValue[] values, String name) throws Exception {
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
        return Mth.clamp(this.getArg(0), this.getArg(1), this.getArg(2));
    }
}
