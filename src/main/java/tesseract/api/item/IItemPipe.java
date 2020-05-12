package tesseract.api.item;

import tesseract.api.IConnectable;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A item pipe is the unit of interaction with item inventories.
 */
@ParametersAreNonnullByDefault
public interface IItemPipe extends IConnectable {

    /**
     * Returns the maximum amount of items that this item component will permit to pass through or be received in a single tick.
     * @return A positive integer representing the maximum packets, zero or negative indicates that this component accepts no items.
     */
    int getCapacity();
}
