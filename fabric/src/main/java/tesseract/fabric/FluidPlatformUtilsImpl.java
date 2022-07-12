package tesseract.fabric;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class FluidPlatformUtilsImpl {
    public static FluidStack createFluidStack(Fluid fluid, long amount){
        return new FluidStack(fluid, amount);
    }

    public static ResourceLocation getStillTexture(Fluid fluid){
        return fluid.getAttributes().getStillTexture();
    }

    public static ResourceLocation getFlowingTexture(Fluid fluid){
        return fluid.getAttributes().getFlowingTexture();
    }
    public static ResourceLocation getFluidId(Fluid fluid){
        return Registry.FLUID.getKey(fluid);
    }

    public static int getFluidTemperature(Fluid fluid){
        return fluid.getAttributes().getTemperature();
    }

    public static boolean isFluidGaseous(Fluid fluid){
        return fluid.getAttributes().isGaseous();
    }

    public static int getFluidColor(Fluid fluid){
        return fluid.getAttributes().getColor();
    }

    public static SoundEvent getFluidSound(Fluid fluid, boolean fill){
        return fill ? fluid.getAttributes().getFillSound() : fluid.getAttributes().getEmptySound();
    }
}
