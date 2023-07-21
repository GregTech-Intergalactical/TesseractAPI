package tesseract.forge;

import earth.terrarium.botarium.common.fluid.base.FluidHolder;
import earth.terrarium.botarium.forge.fluid.ForgeFluidHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundAction;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;

public class FluidPlatformUtilsImpl {

    public static ResourceLocation getStillTexture(Fluid fluid){
        return IClientFluidTypeExtensions.of(fluid).getStillTexture();
    }

    public static ResourceLocation getFlowingTexture(Fluid fluid){
        return IClientFluidTypeExtensions.of(fluid).getFlowingTexture();
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
        return IClientFluidTypeExtensions.of(fluid).getTintColor();
    }

    public static SoundEvent getFluidSound(Fluid fluid, boolean fill){
        return fill ? fluid.getFluidType().getSound(SoundActions.BUCKET_FILL) : fluid.getFluidType().getSound(SoundActions.BUCKET_EMPTY);
    }

    public static Component getFluidDisplayName(FluidHolder fluid){
        return fluid.getFluid().getFluidType().getDescription(fluid);
    }
}
