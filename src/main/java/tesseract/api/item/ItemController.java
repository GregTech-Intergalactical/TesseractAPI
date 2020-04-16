package tesseract.api.item;

import it.unimi.dsi.fastutil.objects.ObjectList;
import tesseract.api.Controller;
import tesseract.graph.*;
import tesseract.util.Dir;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Class acts as a controller in the group of an item components.
 */
public class ItemController extends Controller<ItemConsumer, IItemPipe, IItemNode> {

    /**
     * Creates instance of the controller.
     *
     * @param dim The dimension id.
     * @param group The group this controller handles.
     */
    public ItemController(int dim, @Nonnull Group<IItemPipe, IItemNode> group) {
        super(dim, group);
    }

    @Override
    public void tick() {
    }

    @Override
    public void change() {
    }

    @Override
    protected void onCheck(@Nonnull IItemNode producer, @Nonnull ObjectList<ItemConsumer> consumers, @Nullable Path<IItemPipe> path, long pos) {
    }

    @Override
    protected void onMerge(@Nonnull IItemNode producer, @Nonnull ObjectList<ItemConsumer> consumers) {
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public ITickingController clone(@Nonnull INode group) {
        assert (group instanceof Group<?, ?>);
        return new ItemController(dim, (Group<IItemPipe, IItemNode>) group);
    }

    @Override
    protected boolean isValid(@Nonnull IItemNode producer, @Nullable Dir direction) {
        return false;
    }
}
