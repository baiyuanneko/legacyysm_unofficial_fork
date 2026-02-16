package moe.byn.minecraftmod.legacyysm.event;

import moe.byn.minecraftmod.legacyysm.YesSteveModel;
import moe.byn.minecraftmod.legacyysm.command.RootCommand;
import moe.byn.minecraftmod.legacyysm.command.argument.AnimationArgument;
import moe.byn.minecraftmod.legacyysm.command.argument.ModelsArgument;
import moe.byn.minecraftmod.legacyysm.command.argument.TexturesArgument;
import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = YesSteveModel.MOD_ID)
public final class CommandRegistry {
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, YesSteveModel.MOD_ID);
    public static final DeferredHolder<ArgumentTypeInfo<?, ?>, SingletonArgumentInfo<ModelsArgument>> MODELS_COMMAND_ARGUMENT_TYPE = COMMAND_ARGUMENT_TYPES.register("models", () ->
            ArgumentTypeInfos.registerByClass(ModelsArgument.class, SingletonArgumentInfo.contextFree(ModelsArgument::ids)));
    public static final DeferredHolder<ArgumentTypeInfo<?, ?>, SingletonArgumentInfo<AnimationArgument>> ANIMATIONS_COMMAND_ARGUMENT_TYPE = COMMAND_ARGUMENT_TYPES.register("animations", () ->
            ArgumentTypeInfos.registerByClass(AnimationArgument.class, SingletonArgumentInfo.contextFree(AnimationArgument::animations)));
    public static final DeferredHolder<ArgumentTypeInfo<?, ?>, SingletonArgumentInfo<TexturesArgument>> TEXTURES_COMMAND_ARGUMENT_TYPE = COMMAND_ARGUMENT_TYPES.register("textures", () ->
            ArgumentTypeInfos.registerByClass(TexturesArgument.class, SingletonArgumentInfo.contextFree(TexturesArgument::ids)));

    @SubscribeEvent
    public static void onServerStaring(RegisterCommandsEvent event) {
        RootCommand.register(event.getDispatcher());
    }
}
