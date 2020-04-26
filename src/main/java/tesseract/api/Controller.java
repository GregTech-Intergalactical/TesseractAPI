package tesseract.api;

import tesseract.graph.Group;
import tesseract.graph.IConnectable;
import tesseract.graph.ITickingController;

import javax.annotation.Nonnull;

/**
 * Class acts as a controller in the group of some components.
 */
abstract public class Controller<C extends IConnectable, N extends IConnectable> implements ITickingController {

    protected int tick;
    protected final int dim;
    protected final Group<C, N> group;

    /**
     * Creates instance of the controller.
     *
     * @param dim The dimension id.
     * @param group The group this controller handles.
     */
    protected Controller(int dim, @Nonnull Group<C, N> group) {
        this.dim = dim;
        this.group = group;
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
