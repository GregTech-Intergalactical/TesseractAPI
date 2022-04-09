package tesseract;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.level.LevelAccessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tesseract.api.GraphWrapper;
import tesseract.api.fluid.FluidTransaction;
import tesseract.api.fluid.IFluidNode;
import tesseract.api.fluid.IFluidPipe;
import tesseract.api.gt.GTTransaction;
import tesseract.api.gt.IGTCable;
import tesseract.api.gt.IGTNode;
import tesseract.api.item.IItemNode;
import tesseract.api.item.IItemPipe;
import tesseract.api.item.ItemTransaction;


public class Tesseract {

    public static final String API_ID = "tesseractapi";
    public static final String API_NAME = "Tesseract API";
    public static final String VERSION = "0.0.1";
    public static final String DEPENDS = "";

    public static final Logger LOGGER = LogManager.getLogger(API_ID);



    public static final int HEALTH_CHECK_TIME = 1000;

    public Tesseract() {
    }

    @ExpectPlatform
    public static boolean hadFirstTick(LevelAccessor world) {
        return false;
    }

    @ExpectPlatform
    public static GraphWrapper<GTTransaction, IGTCable, IGTNode> getGT_ENERGY(){
        return null;
    }

    @ExpectPlatform
    public static GraphWrapper<FluidTransaction, IFluidPipe, IFluidNode> getFLUID(){
        return null;
    }

    @ExpectPlatform
    public static GraphWrapper<ItemTransaction, IItemPipe, IItemNode> getITEM(){
        return null;
    }
}
