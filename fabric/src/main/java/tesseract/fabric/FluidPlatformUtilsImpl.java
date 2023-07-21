package tesseract.fabric;

import earth.terrarium.botarium.common.fluid.base.FluidHolder;
import earth.terrarium.botarium.fabric.fluid.holder.FabricFluidHolder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.core.Registry;
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

    public static Component getFluidDisplayName(FluidHolder fluid){
        return FluidVariantAttributes.getName(FluidVariant.of(fluid.getFluid(), fluid.getCompound()));
    }
}
