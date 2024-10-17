package tesseract;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.world.level.LevelAccessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;


public class Tesseract {

    protected final static Set<LevelAccessor> firstTick = new ObjectOpenHashSet<>();

    public static final String API_ID = "tesseractapi";
    public static final String API_NAME = "Tesseract API";
    public static final String VERSION = "0.0.1";
    public static final String DEPENDS = "";

    public static boolean TEST = false;

    public static final Logger LOGGER = LogManager.getLogger(API_ID);



    public static final int HEALTH_CHECK_TIME = 1000;

    public static void init() {
        TesseractGraphWrappers.init();
        TesseractConfig.createConfig();
    }

    public Tesseract() {
    }

    public static boolean hadFirstTick(LevelAccessor world) {
        return TEST || firstTick.contains(world);
    }
}
