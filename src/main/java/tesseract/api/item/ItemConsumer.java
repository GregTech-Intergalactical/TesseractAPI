package tesseract.api.item;

import net.minecraft.item.ItemStack;
import tesseract.api.Consumer;
import tesseract.graph.Path;
import tesseract.util.Dir;

/**
 * A class that acts as a container for a item consumer.
 */
public class ItemConsumer extends Consumer<IItemPipe, IItemNode> {

    private int minCapacity = Integer.MAX_VALUE;
    private final Dir input;

    /**
     * Creates instance of the consumer.
     *
     * @param consumer The consumer node.
     * @param path The path information.
     * @param dir The input direction.
     */
    protected ItemConsumer(IItemNode consumer, Path<IItemPipe> path, Dir dir) {
        super(consumer, path);
        init();
        input = dir;
    }

    /**
     * Inserts an item into an available slot and return the remainder.
     * @param stack ItemData to insert. This must not be modified by the item handler.
     * @param simulate If true, the insertion is only simulated
     * @return The remaining ItemStack that was not inserted (if the entire stack is accepted, then return an empty ItemStack).
     *         May be the same as the input ItemStack if unchanged, otherwise a new ItemStack.
     *         The returned ItemStack can be safely modified after.
     **/
    public int insert(ItemStack stack, boolean simulate) {
        int count = stack.getCount();
        for (int i = 0; i < node.getSlots(); i++) {
            ItemStack inserted = node.insertItem(i, stack, simulate);
            if (inserted.getCount() < stack.getCount()) {
                return inserted.getCount();
            }
        }
        return count;
    }

    /**
     * @param item The Item to be queried.
     * @return If the storage can hold the item (EVER, not at the time of query).
     */
    public boolean canAccept(ItemStack item) {
        return node.canInput(item, input);
    }

    /**
     * @return Returns the priority of this node as a number.
     */
    public int getPriority() {
        return node.getPriority(input);
    }

    /**
     * @return Checks that the min capacity on the path.
     */
    public int getMinCapacity() {
        return minCapacity;
    }

    @Override
    protected void onConnectorCatch(IItemPipe pipe) {
        minCapacity = Math.min(minCapacity, pipe.getCapacity());
    }
}
