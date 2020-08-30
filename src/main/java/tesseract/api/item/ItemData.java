package tesseract.api.item;

/**
 * A class that acts as a wrapper for a item stack and a slot index.
 */
public class ItemData<T> {

    private final int slot;
    private final T stack;

    /**
     * Creates instance of the data.
     *
     * @param slot The slot index.
     * @param stack The ItemStack object.
     */
    public ItemData(int slot, T stack) {
        this.slot = slot;
        this.stack = stack;
    }

    /**
     * @return Gets the slot index.
     */
    public int getSlot() {
        return slot;
    }

    /**
     * @return Gets the ItemStack object.
     */
    public T getStack() {
        return stack;
    }
}
