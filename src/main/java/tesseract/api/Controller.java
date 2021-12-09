package tesseract.api;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import tesseract.graph.Graph;
import tesseract.graph.Group;
import tesseract.graph.INode;
import tesseract.graph.NodeCache;
import tesseract.util.Pos;


/**
 * Class acts as a controller in the group of some components.
 */
abstract public class Controller<T, C extends IConnectable, N> implements ITickingController<T, C, N> {

    protected int tick;
    protected final Level dim;
    protected Group<T, C, N> group;
    protected LongSet visited = new LongOpenHashSet();

    /**
     * Creates instance of the controller.
     *
     * @param supplier The world.
     */
    protected Controller(Level supplier) {
        this.dim = supplier;
    }

    /**
     * Sets the group for the controller.
     *
     * @param container The group this controller handles.
     */
    public Controller<T, C, N> set(INode container) {
        this.group = (Group<T, C, N>) container;
        return this;
    }

    /**
     * Executes on the tick updates.
     */
    @Override
    public void tick() {
        tick++;
        if (tick % 20 == 0) {
            onFrame();
        }
    }
    /**
     * Frame handler, which executes each second.
     */
    protected abstract void onFrame();

    @Override
    public Level getWorld() {
        return this.dim;
    }
}