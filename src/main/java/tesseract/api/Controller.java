package tesseract.api;

import tesseract.graph.Group;
import tesseract.graph.INode;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Class acts as a controller in the group of some components.
 */
@ParametersAreNonnullByDefault
abstract public class Controller<C extends IConnectable, N extends IConnectable> implements ITickingController {

    protected int tick;
    protected final int dim;
    protected Group<C, N> group;

    /**
     * Creates instance of the controller.
     *
     * @param dim The dimension id.
     */
    protected Controller(int dim) {
        this.dim = dim;
    }

    /**
     * Sets the group for the controller.
     *
     * @param container The group this controller handles.
     */
    @SuppressWarnings("unchecked")
    public Controller<C, N> set(INode container) {
        this.group = (Group<C, N>) container;
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
}
