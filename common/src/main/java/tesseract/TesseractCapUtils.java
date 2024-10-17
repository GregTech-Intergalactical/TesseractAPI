package tesseract;

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
import java.util.ServiceLoader;

public interface TesseractCapUtils {
    TesseractCapUtils INSTANCE =  ServiceLoader.load(TesseractCapUtils.class).findFirst().orElseThrow(() -> new IllegalStateException("No implementation of TesseractCapUtils found"));
    //public static final TesseractCapUtils INSTANCE = new TesseractCapUtils();
    Optional<IEnergyHandlerItem> getEnergyHandlerItem(ItemStack stack);
    
    Optional<IEnergyHandlerItem> getWrappedEnergyHandlerItem(ItemStack stack);

    
    Optional<IEnergyHandler> getEnergyHandler(BlockEntity entity, Direction side);

    
    Optional<IHeatHandler> getHeatHandler(BlockEntity entity, Direction side);

    
    Optional<PlatformItemHandler> getItemHandler(BlockEntity entity, Direction side);

    
    Optional<PlatformFluidHandler> getFluidHandler(Level level, BlockPos pos, Direction side);

    
    IFluidNode getFluidNode(Level level, long pos, Direction capSide, Runnable capCallback);

    
    IItemNode getItemNode(Level level, long pos, Direction capSide, Runnable capCallback);
}
