package tesseract.api.item;

import tesseract.graph.IConnectable;

/**
 * A item pipe is the unit of interaction with item inventories.
 */
public interface IItemPipe extends IConnectable {

    /**
     * Returns the maximum amount of items that this item component will permit to pass through or be received in a single tick.
     * @return A positive integer representing the maximum packets, zero or negative indicates that this component accepts no items.
     */
    int getCapacity();

    /**
     * @param capacity The current capacity.
     * @return Checks that the pipe is able to handle single packet.
     */
    default ItemStatus getHandler(int capacity) {
        return (getCapacity() < capacity) ? ItemStatus.FAIL : ItemStatus.SUCCESS;
    }
}
