package tesseract.api.fluid;

import tesseract.api.Controller;
import tesseract.graph.Group;
import tesseract.graph.INode;
import tesseract.graph.ITickingController;

import javax.annotation.Nonnull;

import static tesseract.TesseractAPI.GLOBAL_FLUID_EVENT;

/**
 * Class acts as a controller in the group of a fluid components.
 */
public class FluidController extends Controller<IFluidPipe, IFluidNode> {

    /**
     * Creates instance of the controller.
     *
     * @param dim The dimension id.
     * @param group The group this controller handles.
     */
    public FluidController(int dim, Group<IFluidPipe, IFluidNode> group) {
        super(dim, group);
    }

    @Override
    public void tick() {

    }

    @Override
    public void change() {

    }

    /**
     * Creates new controller for split group.
     *
     * @param group The new group.
     * @return New controller for the group.
     */
    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public ITickingController clone(@Nonnull INode group) {
        assert (group instanceof Group<?, ?>);
        return new FluidController(dim, (Group<IFluidPipe, IFluidNode>) group);
    }
}
