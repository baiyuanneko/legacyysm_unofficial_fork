package moe.byn.minecraftmod.legacyysm.geckolib3.core.molang.expressions;

import moe.byn.minecraftmod.legacyysm.geckolib3.core.molang.MolangParser;
import moe.byn.minecraftmod.legacyysm.mclib.math.Constant;
import moe.byn.minecraftmod.legacyysm.mclib.math.IValue;
import moe.byn.minecraftmod.legacyysm.util.Keep;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class MolangValue extends MolangExpression {
    public IValue value;
    public boolean returns;

    public MolangValue(MolangParser context, IValue value) {
        super(context);
        this.value = value;
    }

    public MolangExpression addReturn() {
        this.returns = true;
        return this;
    }

    @Override
    @Keep
    public double get() {
        return this.value.get();
    }

    @Override
    @Keep
    public String toString() {
        return (this.returns ? MolangParser.RETURN : "") + this.value.toString();
    }

    @Override
    @Keep
    public JsonElement toJson() {
        if (this.value instanceof Constant) {
            return new JsonPrimitive(this.value.get());
        }
        return super.toJson();
    }
}
