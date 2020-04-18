package tesseract.api.item;

import tesseract.api.Consumer;
import tesseract.graph.Path;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A class that acts as a container for a item consumer.
 */
public class ItemConsumer extends Consumer<IItemPipe, IItemNode> {

    private int minCapacity = Integer.MAX_VALUE;

    /**
     * Creates instance of the consumer.
     *
     * @param consumer The consumer node.
     * @param path     The path information.
     */
    protected ItemConsumer(@Nonnull IItemNode consumer, @Nullable Path<IItemPipe> path) {
        super(consumer, path);
    }

    /**
     * <p>
     * Inserts an ItemStack into an available slot and return the remainder.
     * The ItemStack <em>should not</em> be modified in this function!
     * </p>
     * @param stack ItemStack to insert. This must not be modified by the item handler.
     * @param simulate If true, the insertion is only simulated
     * @return The remaining ItemStack that was not inserted (if the entire stack is accepted, then return an empty ItemStack).
     *         May be the same as the input ItemStack if unchanged, otherwise a new ItemStack.
     *         The returned ItemStack can be safely modified after.
     **/
    public int insert(@Nonnull Object stack, boolean simulate) {
        return consumer.insert(stack, simulate);
    }

    /**
     * @param stack ItemStack holding the Item to be queried.
     * @return If the storage can hold the item (EVER, not at the time of query).
     */
    public boolean canAccept(@Nonnull Object stack) {
        return consumer.canAccept(stack);
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
