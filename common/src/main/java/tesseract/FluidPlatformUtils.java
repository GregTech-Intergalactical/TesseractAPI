package tesseract;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

public class FluidPlatformUtils {
    @ExpectPlatform
    public static ResourceLocation getStillTexture(Fluid fluid){
        return null;
    }

    @ExpectPlatform
    public static ResourceLocation getFlowingTexture(Fluid fluid){
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
