package tesseract;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class FluidPlatformUtils {
    @ExpectPlatform
    public static FluidStack createFluidStack(Fluid fluid, long amount){
        return null;
    }

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

    @ExpectPlatform
    public static int getFluidColor(Fluid fluid){
        return 0;
    }

    @ExpectPlatform
    public static SoundEvent getFluidSound(Fluid fluid, boolean fill){
        return null;
    }
}
