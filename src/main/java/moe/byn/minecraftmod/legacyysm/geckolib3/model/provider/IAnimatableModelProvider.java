package moe.byn.minecraftmod.legacyysm.geckolib3.model.provider;

import moe.byn.minecraftmod.legacyysm.util.Keep;
import net.minecraft.resources.ResourceLocation;

public interface IAnimatableModelProvider<E> {
    @Keep
    ResourceLocation getAnimationFileLocation(E animatable);
}