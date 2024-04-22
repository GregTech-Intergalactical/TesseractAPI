package tesseract.fabric;

import earth.terrarium.botarium.api.fluid.FluidHolder;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.material.Fluid;
import tesseract.FluidPlatformUtils;

public class FluidPlatformUtilsImpl extends FluidPlatformUtils {

    public ResourceLocation getStillTexture(Fluid fluid){
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            FluidVariant variant = FluidVariant.of(fluid);
            TextureAtlasSprite[] sprites = FluidVariantRendering.getSprites(variant);

            return sprites[0] == null ? SimpleFluidRenderHandler.WATER_STILL : sprites[0].getName();
        }
        return new ResourceLocation("block/water_still");
    }

    public ResourceLocation getFlowingTexture(Fluid fluid){
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            FluidVariant variant = FluidVariant.of(fluid);
            TextureAtlasSprite[] sprites = FluidVariantRendering.getSprites(variant);

            return sprites[1] == null ? SimpleFluidRenderHandler.WATER_STILL : sprites[1].getName();
        }
        return new ResourceLocation("block/water_still");
    }
    public ResourceLocation getFluidId(Fluid fluid){
        return Registry.FLUID.getKey(fluid);
    }

    public int getFluidTemperature(Fluid fluid){
        return FluidVariantAttributes.getTemperature(FluidVariant.of(fluid));
    }

    public int getFluidDensity(Fluid fluid){
        //cause fabric sucks and doesn't have a good fluid api
        return 1000;
    }

    public boolean isFluidGaseous(Fluid fluid){
        return FluidVariantAttributes.isLighterThanAir(FluidVariant.of(fluid));
    }

    public int getFluidColor(Fluid fluid){
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            return FluidVariantRendering.getColor(FluidVariant.of(fluid));
        }
        return -1;
    }

    public SoundEvent getFluidSound(Fluid fluid, boolean fill){
        return fill ? FluidVariantAttributes.getFillSound(FluidVariant.of(fluid)) : FluidVariantAttributes.getEmptySound(FluidVariant.of(fluid));
    }

    public Component getFluidDisplayName(FluidHolder fluid){
        return FluidVariantAttributes.getName(FluidVariant.of(fluid.getFluid(), fluid.getCompound()));
    }
}
