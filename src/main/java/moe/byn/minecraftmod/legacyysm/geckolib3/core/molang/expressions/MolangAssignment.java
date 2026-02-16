package moe.byn.minecraftmod.legacyysm.geckolib3.core.molang.expressions;

import moe.byn.minecraftmod.legacyysm.geckolib3.core.molang.MolangParser;
import moe.byn.minecraftmod.legacyysm.mclib.math.IValue;
import moe.byn.minecraftmod.legacyysm.mclib.math.Variable;
import moe.byn.minecraftmod.legacyysm.util.Keep;

public class MolangAssignment extends MolangExpression {
    public Variable variable;
    public IValue expression;

    public MolangAssignment(MolangParser context, Variable variable, IValue expression) {
        super(context);
        this.variable = variable;
        this.expression = expression;
    }

    @Override
    @Keep
    public double get() {
        double value = this.expression.get();
        this.variable.set(value);
        return value;
    }

    @Override
    @Keep
    public String toString() {
        return this.variable.getName() + " = " + this.expression.toString();
    }
}
