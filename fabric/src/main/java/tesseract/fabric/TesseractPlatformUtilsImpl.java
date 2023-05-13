package tesseract.fabric;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import tesseract.api.fabric.TileListeners;
import tesseract.api.gt.IEnergyHandler;
import tesseract.api.gt.IGTNode;

import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class TesseractPlatformUtilsImpl {
    public static IGTNode getGTNode(Level level, long pos, Direction direction, Runnable invalidate){
        BlockEntity tile = level.getBlockEntity(BlockPos.of(pos));
        Optional<IEnergyHandler> capability = TesseractCapUtilsImpl.getEnergyHandler(tile, direction);
        if (capability.isPresent()) {
            if (invalidate != null) ((TileListeners)tile).addListener(() -> invalidate.run());
            return capability.get();
        }
        return null;
    }

    public static boolean isFeCap(Class<?> cap){
        return false;
    }

    public static boolean isForge(){
        return false;
    }
}
