package tesseract;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class FluidPlatformUtils {
    @ExpectPlatform
    public static FluidStack createFluidStack(Fluid fluid, long amount){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static ResourceLocation getStillTexture(Fluid fluid){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static ResourceLocation getFlowingTexture(Fluid fluid){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static ResourceLocation getFluidId(Fluid fluid){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static int getFluidTemperature(Fluid fluid){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isFluidGaseous(Fluid fluid){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static int getFluidColor(Fluid fluid){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static SoundEvent getFluidSound(Fluid fluid, boolean fill){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Component getFluidDisplayName(FluidStack fluid){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static FluidStack tryFluidTransfer(IFluidHandler fluidDestination, IFluidHandler fluidSource, long maxAmount, boolean doTransfer){
        throw new AssertionError();
    }
}
