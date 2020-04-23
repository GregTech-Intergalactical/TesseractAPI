package tesseract.api.item;

import it.unimi.dsi.fastutil.objects.ObjectSet;
import tesseract.api.Consumer;
import tesseract.graph.Path;
import tesseract.util.Dir;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A class that acts as a container for a item consumer.
 */
public class ItemConsumer extends Consumer<IItemPipe, IItemNode> {

    private int minCapacity = Integer.MAX_VALUE;
    private final ObjectSet<?> filter;

    /**
     * Creates instance of the consumer.
     *
     * @param consumer The consumer node.
     * @param path The path information.
     * @param dir The added direction.
     */
    protected ItemConsumer(@Nonnull IItemNode consumer, @Nullable Path<IItemPipe> path, @Nonnull Dir dir) {
        super(consumer, path);
        filter = consumer.getInputFilter(dir);
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
        return consumer.insert(data, simulate);
    }

    /**
     * @param item The Item to be queried.
     * @return If the storage can hold the item (EVER, not at the time of query).
     */
    public boolean canQueried(@Nonnull Object item) {
        if (filter.isEmpty()) return consumer.canAccept(item);
        return filter.contains(item) && consumer.canAccept(item);
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
