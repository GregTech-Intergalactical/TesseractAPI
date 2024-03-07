package tesseract.forge;

import earth.terrarium.botarium.common.fluid.base.FluidHolder;
import earth.terrarium.botarium.forge.fluid.ForgeFluidHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.material.Fluid;
import tesseract.FluidPlatformUtils;

public class FluidPlatformUtilsImpl implements FluidPlatformUtils {

    public ResourceLocation getStillTexture(Fluid fluid){
        return fluid.getAttributes().getStillTexture();
    }

    public ResourceLocation getFlowingTexture(Fluid fluid){
        return fluid.getAttributes().getFlowingTexture();
    }
    public ResourceLocation getFluidId(Fluid fluid){
        return fluid.getRegistryName();
    }

    public int getFluidTemperature(Fluid fluid){
        return fluid.getAttributes().getTemperature();
    }

    public int getFluidDensity(Fluid fluid){
        return fluid.getAttributes().getDensity();
    }

    public boolean isFluidGaseous(Fluid fluid){
        return fluid.getAttributes().isGaseous();
    }

    public int getFluidColor(Fluid fluid){
        return fluid.getAttributes().getColor();
    }

    public SoundEvent getFluidSound(Fluid fluid, boolean fill){
        return fill ? fluid.getAttributes().getFillSound() : fluid.getAttributes().getEmptySound();
    }

    public Component getFluidDisplayName(FluidHolder fluid){
        return fluid.getFluid().getAttributes().getDisplayName(ForgeFluidHolder.toStack(fluid));
    }
}
