package tesseract.api.gt;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

public class GTPlatformUtils {
    @ExpectPlatform
    public static IGTNode getNode(Level level, long pos, Direction direction, Runnable invalidate){
        return null;
    }
}
