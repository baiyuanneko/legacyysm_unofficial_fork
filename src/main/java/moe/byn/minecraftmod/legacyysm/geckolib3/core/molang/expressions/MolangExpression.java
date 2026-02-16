package moe.byn.minecraftmod.legacyysm.geckolib3.core.molang.expressions;

import moe.byn.minecraftmod.legacyysm.geckolib3.core.molang.MolangParser;
import moe.byn.minecraftmod.legacyysm.mclib.math.Constant;
import moe.byn.minecraftmod.legacyysm.mclib.math.IValue;
import moe.byn.minecraftmod.legacyysm.mclib.math.Operation;
import moe.byn.minecraftmod.legacyysm.util.Keep;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public abstract class MolangExpression implements IValue {
    public MolangParser context;

    public MolangExpression(MolangParser context) {
        this.context = context;
    }

    public static boolean isZero(MolangExpression expression) {
        return isConstant(expression, 0);
    }

    public static boolean isOne(MolangExpression expression) {
        return isConstant(expression, 1);
    }

    public static boolean isConstant(MolangExpression expression, double x) {
        if (expression instanceof MolangValue value) {
            return value.value instanceof Constant && Operation.equals(value.value.get(), x);
        }
        return false;
    }

    public static boolean isExpressionConstant(MolangExpression expression) {
        if (expression instanceof MolangValue value) {
            return value.value instanceof Constant;
        }
        return false;
    }

    @Keep
    public JsonElement toJson() {
        return new JsonPrimitive(this.toString());
    }
}
