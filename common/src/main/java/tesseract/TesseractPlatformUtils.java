package tesseract;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import tesseract.api.gt.IEnergyHandler;
import tesseract.api.gt.IGTNode;

import javax.annotation.Nullable;

public class TesseractPlatformUtils {
    @ExpectPlatform
    public static IGTNode getGTNode(Level level, long pos, Direction direction, Runnable invalidate){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isFeCap(Class<?> cap){
        throw new AssertionError();
    }


    @ExpectPlatform
    public static boolean isForge(){
        throw new AssertionError();
    }
}
