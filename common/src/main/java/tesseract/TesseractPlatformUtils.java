package tesseract;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandler;
import tesseract.api.fluid.IFluidNode;
import tesseract.api.gt.IEnergyHandler;
import tesseract.api.gt.IGTNode;
import tesseract.api.item.IItemNode;

import javax.annotation.Nullable;

public class TesseractPlatformUtils {
    @ExpectPlatform
    public static IGTNode getGTNode(Level level, long pos, Direction direction, Runnable invalidate){
        return null;
    }

    @ExpectPlatform
    public static LazyOptional<IItemHandler> getItemHandler(BlockEntity be, @Nullable Direction side){
        return LazyOptional.empty();
    }

    @ExpectPlatform
    public static LazyOptional<IFluidHandler> getFluidHandler(BlockEntity be, @Nullable Direction side){
        return LazyOptional.empty();
    }

    public static<T> LazyOptional<T> getCapability(BlockEntity be, Capability<T> capability, @Nullable Direction side){
        if (be instanceof ICapabilityProvider provider){
            return provider.getCapability(capability, side);
        }
        return LazyOptional.empty();
    }

    @ExpectPlatform
    public static LazyOptional<IFluidHandlerItem> getFluidHandlerItem(ItemStack stack) {
        return LazyOptional.empty();
    }

    @ExpectPlatform
    public static LazyOptional<IEnergyHandler> getEnergyHandlerItem(ItemStack stack){
        return LazyOptional.empty();
    }

    @ExpectPlatform
    public static ResourceLocation getFluidId(Fluid fluid){
        return null;
    }

    @ExpectPlatform
    public static int getFluidTemperature(Fluid fluid){
        return 0;
    }

    @ExpectPlatform
    public static boolean isFluidGaseous(Fluid fluid){
        return false;
    }
}
