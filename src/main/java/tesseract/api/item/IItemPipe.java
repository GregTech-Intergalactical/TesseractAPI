package tesseract.api.item;

import tesseract.api.IConnectable;
import tesseract.api.ITransactionModifier;

/**
 * A item pipe is the unit of interaction with item inventories.
 */
public interface IItemPipe extends IConnectable, ITransactionModifier {

    /**
     * Returns the maximum amount of items that this item component will permit to pass through or be received in a single tick.
     *
     * @return A positive integer representing the maximum packets, zero or negative indicates that this component accepts no items.
     */
    int getCapacity();
}
