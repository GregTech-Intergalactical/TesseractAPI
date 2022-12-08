package tesseract.fabric;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import tesseract.api.gt.IEnergyHandler;
import tesseract.api.gt.IGTNode;

@SuppressWarnings("UnstableApiUsage")
public class TesseractPlatformUtilsImpl {
    public static IGTNode getGTNode(Level level, long pos, Direction direction, Runnable invalidate){
        BlockEntity tile = level.getBlockEntity(BlockPos.of(pos));
        LazyOptional<IEnergyHandler> capability = TesseractCapUtilsImpl.getEnergyHandler(tile, direction).map(e -> LazyOptional.of(() -> e)).orElse(LazyOptional.empty());
        if (capability.isPresent()) {
            if (invalidate != null) capability.addListener(t -> invalidate.run());
            return capability.resolve().get();
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
