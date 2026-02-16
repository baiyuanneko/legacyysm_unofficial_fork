package moe.byn.minecraftmod.legacyysm;

import moe.byn.minecraftmod.legacyysm.capability.YSMAttachments;
import moe.byn.minecraftmod.legacyysm.config.GeneralConfig;
import moe.byn.minecraftmod.legacyysm.event.CommandRegistry;
import moe.byn.minecraftmod.legacyysm.model.ServerModelManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(YesSteveModel.MOD_ID)
public class YesSteveModel {
    public static final String MOD_ID = "legacyysm_byn";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    public YesSteveModel(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, GeneralConfig.init());
        ServerModelManager.reloadPacks();
        CommandRegistry.COMMAND_ARGUMENT_TYPES.register(modEventBus);
        YSMAttachments.ATTACHMENTS.register(modEventBus);
    }
}