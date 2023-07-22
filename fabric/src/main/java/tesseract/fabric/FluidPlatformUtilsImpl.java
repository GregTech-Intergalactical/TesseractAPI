package tesseract.fabric;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import earth.terrarium.botarium.common.fluid.base.FluidHolder;
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

public class FluidPlatformUtilsImpl {

    public static ResourceLocation getStillTexture(Fluid fluid){
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            FluidVariant variant = FluidVariant.of(fluid);
            TextureAtlasSprite[] sprites = FluidVariantRendering.getSprites(variant);

            return sprites[0] == null ? SimpleFluidRenderHandler.WATER_STILL : sprites[0].getName();
        }
        return new ResourceLocation("block/water_still");
    }

    public static ResourceLocation getFlowingTexture(Fluid fluid){
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            FluidVariant variant = FluidVariant.of(fluid);
            TextureAtlasSprite[] sprites = FluidVariantRendering.getSprites(variant);

            return sprites[1] == null ? SimpleFluidRenderHandler.WATER_STILL : sprites[1].getName();
        }
        return new ResourceLocation("block/water_still");
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
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            return FluidVariantRendering.getColor(FluidVariant.of(fluid));
        }
        return -1;
    }

    public static SoundEvent getFluidSound(Fluid fluid, boolean fill){
        return fill ? FluidVariantAttributes.getFillSound(FluidVariant.of(fluid)) : FluidVariantAttributes.getEmptySound(FluidVariant.of(fluid));
    }

    public static Component getFluidDisplayName(FluidHolder fluid){
        return FluidVariantAttributes.getName(FluidVariant.of(fluid.getFluid(), fluid.getCompound()));
    }
}
