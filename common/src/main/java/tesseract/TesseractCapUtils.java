package tesseract;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import tesseract.api.fluid.IFluidNode;
import tesseract.api.gt.IEnergyHandler;
import tesseract.api.gt.IEnergyHandlerItem;
import tesseract.api.heat.IHeatHandler;

import java.util.Optional;

public class TesseractCapUtils {
    @ExpectPlatform
    public static Optional<IEnergyHandlerItem> getEnergyHandlerItem(ItemStack stack){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Optional<IEnergyHandler> getEnergyHandler(BlockEntity entity, Direction side){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Optional<IHeatHandler> getHeatHandler(BlockEntity entity, Direction side){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Optional<IItemHandler> getItemHandler(BlockEntity entity, Direction side){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static LazyOptional<IItemHandler> getLazyItemHandler(BlockEntity entity, Direction side){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static IFluidNode getFluidNode(Level level, long pos, Direction capSide, Runnable capCallback){
        throw new AssertionError();
    }
}
