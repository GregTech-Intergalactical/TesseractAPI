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
        throw new AssertionError();
    }
    @ExpectPlatform
    public static LazyOptional<IEnergyHandler> getWrappedHandler(BlockEntity be, @Nullable Direction side){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isFeCap(Capability<?> cap){
        throw new AssertionError();
    }


    @ExpectPlatform
    public static LazyOptional<IEnergyHandler> getEnergyHandlerItem(ItemStack stack){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isForge(){
        throw new AssertionError();
    }

}
