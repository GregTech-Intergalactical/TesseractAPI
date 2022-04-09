package tesseract;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import tesseract.api.fluid.IFluidNode;
import tesseract.api.gt.IGTNode;
import tesseract.api.item.IItemNode;

public class TesseractPlatformUtils {
    @ExpectPlatform
    public static IGTNode getGTNode(Level level, long pos, Direction direction, Runnable invalidate){
        return null;
    }

    @ExpectPlatform
    public static IItemNode getItemNode(Level level, long pos, Direction capSide, Runnable capCallback){
        return null;
    }

    @ExpectPlatform
    public static IFluidNode getFluidNode(Level level, long pos, Direction capSide, Runnable capCallback){
        return null;
    }

    @ExpectPlatform
    public static ResourceLocation getFluidId(Fluid fluid){
        return null;
    }

    @ExpectPlatform
    public static int getFluidTemperature(Fluid fluid){
        return 0;
    }

    @ExpectPlatform
    public static boolean isFluidGaseous(Fluid fluid){
        return false;
    }
}
