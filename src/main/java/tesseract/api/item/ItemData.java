package tesseract.api.item;

import javax.annotation.Nonnull;

/**
 * A class that acts as a wrapper for a item stack and a slot index.
 */
public class ItemData {

    private final int slot;
    private final Object stack;

    /**
     * Creates instance of the data.
     *
     * @param slot The slot index.
     * @param stack The ItemStack object.
     */
    public ItemData(int slot, @Nonnull Object stack) {
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
    @Nonnull
    public Object getStack() {
        return stack;
    }
}
