package tesseract.forge;

import earth.terrarium.botarium.common.fluid.base.FluidHolder;
import earth.terrarium.botarium.forge.fluid.ForgeFluidHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.material.Fluid;

public class FluidPlatformUtilsImpl {

    public static ResourceLocation getStillTexture(Fluid fluid){
        return fluid.getAttributes().getStillTexture();
    }

    public static ResourceLocation getFlowingTexture(Fluid fluid){
        return fluid.getAttributes().getFlowingTexture();
    }
    public static ResourceLocation getFluidId(Fluid fluid){
        return fluid.getRegistryName();
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

    public static Component getFluidDisplayName(FluidHolder fluid){
        return fluid.getFluid().getAttributes().getDisplayName(ForgeFluidHolder.toStack(fluid));
    }
}
