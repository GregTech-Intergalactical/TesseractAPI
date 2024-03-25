package tesseract;

import carbonconfiglib.config.Config;
import carbonconfiglib.config.ConfigEntry;
import carbonconfiglib.config.ConfigHandler;
import carbonconfiglib.config.ConfigSection;

public class TesseractConfig {

    public static ConfigEntry.DoubleValue EU_TO_FE_RATIO, EU_TO_TRE_RATIO;
    public static ConfigEntry.BoolValue ENABLE_FE_OR_TRE_INPUT, ENABLE_MI_COMPAT;

    public static ConfigHandler CONFIG;

    public static void createConfig(){
        Config config = new Config(Tesseract.API_ID);
        ConfigSection section = config.add("general");
        EU_TO_FE_RATIO = section.addDouble("eu_to_fe_ratio", 4.0, "The ratio of the eu to the fe energy converting - Default: (1.0 EU = 4.0 FE)").setMin(Double.MIN_VALUE);
        EU_TO_TRE_RATIO = section.addDouble("eu_to_tre_ratio", 1.0, "The ratio of the eu to the tre energy converting - Default: (1.0 EU = 1.0 TRE)").setMin(Double.MIN_VALUE);
        ENABLE_FE_OR_TRE_INPUT = section.addBool("enable_fe_or_tre_input", !TesseractPlatformUtils.INSTANCE.isForge(), "Enables EU Machines and cables being able to input FE or TRE(Tech Reborn Energy),",
                "Please do not enable on forge unless you have balanced the fe compat to not be broken af due to power creep. - Default: false on forge, true on fabric");
        ENABLE_MI_COMPAT = section.addBool("enabled_mi_compat", true, "Enables Tesseract EU having compat with MI energy. - Default: true");
        CONFIG = TesseractPlatformUtils.INSTANCE.createConfig(config);
        CONFIG.register();
    }
}
