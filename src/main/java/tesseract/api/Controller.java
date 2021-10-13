package tesseract.api;

import net.minecraft.util.Direction;
import net.minecraft.world.World;
import tesseract.graph.Group;
import tesseract.graph.INode;

import java.util.function.Function;


/**
 * Class acts as a controller in the group of some components.
 */
abstract public class Controller<T, C extends IConnectable, N> implements ITickingController<T, C, N> {

    protected int tick;
    protected final World dim;
    protected Group<T, C, N> group;
    public final Function<C, N> wrapper;
    /**
     * Creates instance of the controller.
     * @param wrapper the function to wrap pipes in a node.
     * @param supplier The world.
     */
    protected Controller(final Function<C, N> wrapper, World supplier) {
        this.dim = supplier;
        this.wrapper = wrapper;
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

    protected Direction getMapDirection(long pos, Direction def) {
        return group.getNodes().get(pos).isPipe() ? Direction.NORTH : def;
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

    @Override
    public N wrapPipe(C pipe) {
        return this.wrapper.apply(pipe);
    }
}