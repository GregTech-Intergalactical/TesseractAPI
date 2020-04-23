package tesseract.api.item;

import tesseract.api.NodeWrapper;
import tesseract.graph.Path;
import tesseract.util.Dir;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A class that acts as a container for a item consumer.
 */
public class ItemConsumer extends NodeWrapper<IItemPipe, IItemNode> {

    private int minCapacity = Integer.MAX_VALUE;
    private final Dir input;

    /**
     * Creates instance of the consumer.
     *
     * @param consumer The consumer node.
     * @param path The path information.
     * @param dir The input direction.
     */
    protected ItemConsumer(@Nonnull IItemNode consumer, @Nullable Path<IItemPipe> path, @Nonnull Dir dir) {
        super(consumer, path);
        input = dir;
    }

    /**
     * Inserts an item into an available slot and return the remainder.
     * @param data ItemData to insert. This must not be modified by the item handler.
     * @param simulate If true, the insertion is only simulated
     * @return The remaining ItemStack that was not inserted (if the entire stack is accepted, then return an empty ItemStack).
     *         May be the same as the input ItemStack if unchanged, otherwise a new ItemStack.
     *         The returned ItemStack can be safely modified after.
     **/
    public int insert(@Nonnull ItemData data, boolean simulate) {
        return node.insert(data, simulate);
    }

    /**
     * @param item The Item to be queried.
     * @return If the storage can hold the item (EVER, not at the time of query).
     */
    public boolean canAccept(@Nonnull Object item) {
        return node.canInput(item, input);
    }

    /**
     * @return Checks that the min capacity on the path.
     */
    public int getMinCapacity() {
        return minCapacity;
    }

    @Override
    protected void onConnectorCatch(@Nonnull IItemPipe pipe) {
        minCapacity = Math.min(minCapacity, pipe.getCapacity());
    }
}
