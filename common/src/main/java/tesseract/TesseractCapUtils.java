package tesseract;

import dev.architectury.injectables.annotations.ExpectPlatform;
import earth.terrarium.botarium.common.fluid.base.PlatformFluidHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import tesseract.api.fluid.IFluidNode;
import tesseract.api.gt.IEnergyHandler;
import tesseract.api.gt.IEnergyHandlerItem;
import tesseract.api.heat.IHeatHandler;
import tesseract.api.item.IItemNode;
import tesseract.api.item.PlatformItemHandler;

import java.util.Optional;

public class TesseractCapUtils {
    @ExpectPlatform
    public static Optional<IEnergyHandlerItem> getEnergyHandlerItem(ItemStack stack){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Optional<IEnergyHandlerItem> getWrappedEnergyHandlerItem(ItemStack stack){
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
    public static Optional<PlatformItemHandler> getItemHandler(BlockEntity entity, Direction side){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Optional<PlatformFluidHandler> getFluidHandler(Level level, BlockPos pos, Direction side){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static IFluidNode getFluidNode(Level level, long pos, Direction capSide, Runnable capCallback){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static IItemNode getItemNode(Level level, long pos, Direction capSide, Runnable capCallback){
        throw new AssertionError();
    }
}
