package tesseract.api.item;

import tesseract.api.Controller;
import tesseract.graph.Group;
import tesseract.graph.INode;
import tesseract.graph.ITickingController;

import javax.annotation.Nonnull;

/**
 * Class acts as a controller in the group of an item components.
 */
public class ItemController extends Controller<IItemPipe, IItemNode> {

    /**
     * Creates instance of the controller.
     *
     * @param dim The dimension id.
     * @param group The group this controller handles.
     */
    public ItemController(int dim, Group<IItemPipe, IItemNode> group) {
        super(dim, group);
    }

    @Override
    public void tick() {
        //TODO: Finish tick method
    }

    @Override
    public void change() {
        //TODO: Finish change method
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
        return new ItemController(dim, (Group<IItemPipe, IItemNode>) group);
    }
}
