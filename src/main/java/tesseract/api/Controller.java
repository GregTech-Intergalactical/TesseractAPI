package tesseract.api;

import net.minecraft.world.World;
import tesseract.graph.Graph;
import tesseract.graph.Group;
import tesseract.graph.INode;


/**
 * Class acts as a controller in the group of some components.
 */
abstract public class Controller<T, C extends IConnectable, N> implements ITickingController<T, C, N> {

    protected int tick;
    protected final World dim;
    protected Group<T, C, N> group;
    protected final Graph.INodeGetter<N> getter;

    /**
     * Creates instance of the controller.
     *
     * @param supplier The world.
     */
    protected Controller(World supplier, Graph.INodeGetter<N> getter) {
        this.dim = supplier;
        this.getter = getter;
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
    public World getWorld() {
        return this.dim;
    }
}