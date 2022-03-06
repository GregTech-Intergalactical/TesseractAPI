package tesseract;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import tesseract.api.gt.IGTNode;
import tesseract.api.item.IItemNode;
import tesseract.graph.INode;

public class TesseractPlatformUtils {
    @ExpectPlatform
    public static IGTNode getGTNode(Level level, long pos, Direction direction, Runnable invalidate){
        return null;
    }

    @ExpectPlatform
    public static IItemNode getItemNode(Level level, long pos, Direction capSide, Runnable capCallback){
        return null;
    }
}
