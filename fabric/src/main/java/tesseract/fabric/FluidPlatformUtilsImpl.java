package tesseract.fabric;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

public class FluidPlatformUtilsImpl {
    public static ResourceLocation getFluidId(Fluid fluid){
        return Registry.FLUID.getKey(fluid);
    }

    public static int getFluidTemperature(Fluid fluid){
        return 0;
    }

    public static boolean isFluidGaseous(Fluid fluid){
        return false;
    }
}
