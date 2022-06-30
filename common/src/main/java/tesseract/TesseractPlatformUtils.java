package tesseract;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandler;
import tesseract.api.gt.IEnergyHandler;
import tesseract.api.gt.IGTNode;

import javax.annotation.Nullable;

public class TesseractPlatformUtils {
    @ExpectPlatform
    public static IGTNode getGTNode(Level level, long pos, Direction direction, Runnable invalidate){
        return null;
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
    public static boolean isForge(){
        return false;
    }

}
