package tesseract;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

import java.sql.Ref;

public class TesseractConfig {

    public static final Common COMMON = new Common();

    public static final CommonConfig COMMON_CONFIG;
    public static final ForgeConfigSpec COMMON_SPEC;

    static {

        final Pair<CommonConfig, ForgeConfigSpec> COMMON_PAIR = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
        COMMON_CONFIG = COMMON_PAIR.getLeft();
        COMMON_SPEC = COMMON_PAIR.getRight();

    }

    public static void onModConfigEvent(final ModConfig e) {
        if (e.getModId().equals(Tesseract.API_ID)){
            if (e.getSpec() == COMMON_SPEC) bakeCommonConfig();
        }
    }


    public static class  Common {
        public double EU_TO_FE_RATIO, EU_TO_MI_RATIO, EU_TO_TRE_RATIO;
        public boolean ENABLE_FE_OR_TRE_INPUT;
    }


    public static class CommonConfig {

        public final ForgeConfigSpec.DoubleValue EU_TO_FE_RATIO, EU_TO_MI_RATIO, EU_TO_TRE_RATIO;

        public final ForgeConfigSpec.BooleanValue ENABLE_FE_OR_TRE_INPUT;

        public CommonConfig(ForgeConfigSpec.Builder builder) {

            EU_TO_FE_RATIO = builder.comment("The ratio of the eu to the fe energy converting - Default: (1.0 EU = 8.0 FE)")
                    .translation(Tesseract.API_ID + ".config.eu_to_rf_ratio")
                    .defineInRange("EU_TO_FE_RATIO", 8.0D, 0.1D, (Double.MAX_VALUE));

            EU_TO_TRE_RATIO = builder.comment("The ratio of the eu to the tre energy converting - Default: (1.0 EU = 1.0 TRE)")
                    .translation(Tesseract.API_ID + ".config.eu_to_tre_ratio")
                    .defineInRange("EU_TO_TRE_RATIO", 1.0D, 0.1D, (Double.MAX_VALUE));

            EU_TO_MI_RATIO = builder.comment("The ratio of the eu to the mi energy converting - Default: (1.0 EU = 1.0 MI EU)")
                    .translation(Tesseract.API_ID + ".config.eu_to_tre_ratio")
                    .defineInRange("EU_TO_TRE_RATIO", 1.0D, 0.1D, (Double.MAX_VALUE));

            ENABLE_FE_OR_TRE_INPUT = builder.comment("Enables GT Machines and  cables being able to input FE or TRE(Tech Reborn Energy),",
                            "Please do not enable unless you have balanced the fe compat to not be broken due to power creep. - Default: false")
                    .translation(Tesseract.API_ID + ".config.eenable_fe_or_tre_input")
                    .define("ENABLE_FE_OR_TRE_INPUT", false);
        }

    }

    private static void bakeCommonConfig() {
        COMMON.EU_TO_FE_RATIO = COMMON_CONFIG.EU_TO_FE_RATIO.get();
        COMMON.EU_TO_TRE_RATIO = COMMON_CONFIG.EU_TO_TRE_RATIO.get();
        COMMON.ENABLE_FE_OR_TRE_INPUT = COMMON_CONFIG.ENABLE_FE_OR_TRE_INPUT.get();
        COMMON.EU_TO_MI_RATIO = COMMON_CONFIG.EU_TO_MI_RATIO.get();
    }
}
