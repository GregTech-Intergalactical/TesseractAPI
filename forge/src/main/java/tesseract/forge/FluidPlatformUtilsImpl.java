package tesseract.forge;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.SoundAction;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;

public class FluidPlatformUtilsImpl {
    public static FluidStack createFluidStack(Fluid fluid, long amount){
        return new FluidStack(fluid, (int) amount);
    }

    public static ResourceLocation getStillTexture(Fluid fluid){
        return fluid.getFluidType().getStillTexture();
    }

    public static ResourceLocation getFlowingTexture(Fluid fluid){
        return fluid.getFluidType().getFlowingTexture();
    }
    public static ResourceLocation getFluidId(Fluid fluid){
        return ForgeRegistries.FLUIDS.getKey(fluid);
    }

    public static int getFluidTemperature(Fluid fluid){
        return fluid.getFluidType().getTemperature();
    }

    public static boolean isFluidGaseous(Fluid fluid){
        return fluid.getFluidType().isLighterThanAir();
    }

    public static int getFluidColor(Fluid fluid){
        return fluid.getFluidType().getColor();
    }

    public static SoundEvent getFluidSound(Fluid fluid, boolean fill){
        return fill ? fluid.getFluidType().getSound(SoundActions.BUCKET_FILL) : fluid.getFluidType().getSound(SoundActions.BUCKET_EMPTY);
    }

    public static Component getFluidDisplayName(FluidStack fluid){
        return fluid.getFluid().getFluidType().getDescription(fluid);
    }

    public static FluidStack tryFluidTransfer(IFluidHandler fluidDestination, IFluidHandler fluidSource, long maxAmount, boolean doTransfer){
        return FluidUtil.tryFluidTransfer(fluidDestination, fluidSource, (int) maxAmount, doTransfer);
    }
}
