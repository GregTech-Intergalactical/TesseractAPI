package tesseract.api;

import tesseract.graph.Group;
import tesseract.graph.IConnectable;
import tesseract.graph.ITickingController;

/**
 * Class acts as a controller in the group of components.
 */
abstract public class Controller<C extends IConnectable, N extends IConnectable> implements ITickingController {

    protected int dim;
    protected Group<C, N> group;

    /**
     * Creates instance of the controller.
     *
     * @param dim The dimension id.
     * @param group The group this controller handles.
     */
    protected Controller(int dim, Group<C, N> group) {
        this.dim = dim;
        this.group = group;
    }
}
