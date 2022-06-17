package tesseract.forge;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

public class FluidPlatformUtilsImpl {
    public static ResourceLocation getFluidId(Fluid fluid){
        return fluid.getRegistryName();
    }

    public static int getFluidTemperature(Fluid fluid){
        return fluid.getAttributes().getTemperature();
    }

    public static boolean isFluidGaseous(Fluid fluid){
        return fluid.getAttributes().isGaseous();
    }
}
