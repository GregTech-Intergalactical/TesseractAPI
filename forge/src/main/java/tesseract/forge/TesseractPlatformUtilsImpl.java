package tesseract.forge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import tesseract.api.gt.IEnergyHandler;
import tesseract.api.gt.IGTNode;

public class TesseractPlatformUtilsImpl {
    public static IGTNode getGTNode(Level level, long pos, Direction direction, Runnable invalidate){
        BlockEntity tile = level.getBlockEntity(BlockPos.of(pos));
        LazyOptional<IEnergyHandler> capability = TesseractCapUtilsImpl.getEnergyHandler(tile, direction).map(e -> LazyOptional.of(() -> e)).orElse(LazyOptional.empty());
        if (capability.isPresent()) {
            if (invalidate != null )capability.addListener(o -> invalidate.run());
            return capability.resolve().get();
        }
        return null;
    }

    public static boolean isFeCap(Class<?> cap){
        return cap == IEnergyStorage.class;
    }

    public static boolean isForge(){
        return true;
    }
}
