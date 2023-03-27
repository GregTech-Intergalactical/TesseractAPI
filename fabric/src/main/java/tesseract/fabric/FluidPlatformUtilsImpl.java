package tesseract.fabric;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

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
        return FluidVariantAttributes.getHandler(fluid).getTemperature(FluidVariant.of(fluid));
    }

    public static boolean isFluidGaseous(Fluid fluid){
        return FluidVariantAttributes.getHandler(fluid).isLighterThanAir(FluidVariant.of(fluid));
    }

    public static int getFluidColor(Fluid fluid){
        return fluid.getAttributes().getColor();
    }

    public static SoundEvent getFluidSound(Fluid fluid, boolean fill){
        return fill ? FluidVariantAttributes.getFillSound(FluidVariant.of(fluid)) : FluidVariantAttributes.getEmptySound(FluidVariant.of(fluid));
    }

    public static Component getFluidDisplayName(FluidStack fluid){
        return FluidVariantAttributes.getName(fluid.getType());
    }

    public static FluidStack tryFluidTransfer(IFluidHandler fluidDestination, IFluidHandler fluidSource, long maxAmount, boolean doTransfer){
        return FluidUtil.tryFluidTransfer(fluidDestination, fluidSource, maxAmount, doTransfer);
    }
}
