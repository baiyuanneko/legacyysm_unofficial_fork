package moe.byn.minecraftmod.legacyysm.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class GeneralConfig {
    public static ModConfigSpec CONFIG_SPEC;
    public static ModConfigSpec.BooleanValue DISCLAIMER_SHOW;
    public static ModConfigSpec.BooleanValue PRINT_ANIMATION_ROULETTE_MSG;
    public static ModConfigSpec.BooleanValue DISABLE_SELF_MODEL;
    public static ModConfigSpec.BooleanValue DISABLE_OTHER_MODEL;
    public static ModConfigSpec.BooleanValue DISABLE_SELF_HANDS;
    public static ModConfigSpec.ConfigValue<String> DEFAULT_MODEL_ID;
    public static ModConfigSpec.ConfigValue<String> DEFAULT_MODEL_TEXTURE;

    public static ModConfigSpec init() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        init(builder);
        ExtraPlayerScreenConfig.init(builder);
        CONFIG_SPEC = builder.build();
        return CONFIG_SPEC;
    }

    public static void init(ModConfigSpec.Builder builder) {
        builder.push("general");

        builder.comment("Whether to display disclaimer GUI");
        DISCLAIMER_SHOW = builder.define("DisclaimerShow", true);

        builder.comment("Whether to print animation roulette play message");
        PRINT_ANIMATION_ROULETTE_MSG = builder.define("PrintAnimationRouletteMsg", true);

        builder.comment("Prevents rendering of self player's model");
        DISABLE_SELF_MODEL = builder.define("DisableSelfModel", false);

        builder.comment("Prevents rendering of other player's model");
        DISABLE_OTHER_MODEL = builder.define("DisableOtherModel", false);

        builder.comment("Prevents rendering of self player's hand");
        DISABLE_SELF_HANDS = builder.define("DisableSelfHands", false);

        builder.comment("The default model ID when a player first enters the game");
        DEFAULT_MODEL_ID = builder.define("DefaultModelId", "default");

        builder.comment("The default model texture when a player first enters the game");
        DEFAULT_MODEL_TEXTURE = builder.define("DefaultModelTexture", "default.png");

        builder.pop();
    }
}
