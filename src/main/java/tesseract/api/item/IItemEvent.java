package tesseract.api.item;

import javax.annotation.Nonnull;

/**
 * Interface for handling an item events. (Controller will handle them)
 */
public interface IItemEvent {

    /**
     * Executes when the cable trying to transport higher amount of items than can.
     * @param dim The dimension id.
     * @param pos The pipe position.
     * @param capacity The current capacity.
     * @param stacks ItemStacks holding the Item to be queried.
     */
    void onPipeOverCapacity(int dim, long pos, int capacity, @Nonnull Object[] stacks);
}
