package moe.byn.minecraftmod.legacyysm.command.argument;

import moe.byn.minecraftmod.legacyysm.util.Keep;
import com.google.common.collect.Sets;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class AnimationArgument implements ArgumentType<String> {
    private static final Collection<String> EXAMPLES = Collections.singleton("idle");
    private static final String STOP = "stop";

    private AnimationArgument() {
    }

    public static AnimationArgument animations() {
        return new AnimationArgument();
    }

    public static String getAnimation(CommandContext<CommandSourceStack> context, String name) {
        return context.getArgument(name, String.class);
    }

    @Override
    @Keep
    public String parse(StringReader reader) throws CommandSyntaxException {
        return reader.readString();
    }

    @Override
    @Keep
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> source, SuggestionsBuilder builder) {
        if (source.getSource() instanceof SharedSuggestionProvider) {
            if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
                return Suggestions.empty();
            } else {
                Set<String> animations = getClientAnimations();
                animations.add(STOP);
                return SharedSuggestionProvider.suggest(animations, builder);
            }
        } else {
            return Suggestions.empty();
        }
    }
    
    @SuppressWarnings("unchecked")
    private static Set<String> getClientAnimations() {
        Set<String> animations = Sets.newHashSet();
        try {
            Class<?> geckoLibCacheClass = Class.forName("moe.byn.minecraftmod.legacyysm.geckolib3.resource.GeckoLibCache");
            java.lang.reflect.Method getInstanceMethod = geckoLibCacheClass.getMethod("getInstance");
            Object cacheInstance = getInstanceMethod.invoke(null);
            java.lang.reflect.Method getAnimationsMethod = geckoLibCacheClass.getMethod("getAnimations");
            java.util.Map<?, ?> animationsMap = (java.util.Map<?, ?>) getAnimationsMethod.invoke(cacheInstance);
            
            Class<?> customPlayerModelClass = Class.forName("moe.byn.minecraftmod.legacyysm.client.model.CustomPlayerModel");
            java.lang.reflect.Field defaultAnimField = customPlayerModelClass.getField("DEFAULT_MAIN_ANIMATION");
            Object defaultAnimKey = defaultAnimField.get(null);
            
            Object animationFile = animationsMap.get(defaultAnimKey);
            if (animationFile != null) {
                java.lang.reflect.Method animationsMethod = animationFile.getClass().getMethod("animations");
                java.util.Map<String, ?> animMap = (java.util.Map<String, ?>) animationsMethod.invoke(animationFile);
                animations.addAll(animMap.keySet());
            }
        } catch (Exception e) {
        }
        return animations;
    }

    @Override
    @Keep
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
