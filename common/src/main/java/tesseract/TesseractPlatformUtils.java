package tesseract;

import carbonconfiglib.config.Config;
import carbonconfiglib.config.ConfigHandler;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import tesseract.api.gt.IGTNode;
import tesseract.api.heat.IHeatNode;
import tesseract.api.rf.IRFNode;
import tesseract.graph.INode;

import java.util.ServiceLoader;

public interface TesseractPlatformUtils {
    TesseractPlatformUtils INSTANCE =  ServiceLoader.load(TesseractPlatformUtils.class).findFirst().orElseThrow(() -> new IllegalStateException("No implementation of TesseractPlatformUtils found"));


    IGTNode getGTNode(Level level, long pos, Direction direction, Runnable invalidate);

    IRFNode getRFNode(Level level, long pos, Direction direction, Runnable invalidate);

    IHeatNode getHeatNode(Level level, long pos, Direction direction, Runnable invalidate);

    boolean isFeCap(Class<?> cap);


    boolean isForge();

    ConfigHandler createConfig(Config config);

    boolean areCapsCompatible(ItemStack a, ItemStack b);
}
