package tesseract.api.item;

import javax.annotation.Nonnull;

/**
 * A class that acts as a wrapper for a item stack and a slot index.
 */
public class ItemData {

    public final int slot;
    public final int count;
    public final Object stack;

    /**
     * Creates instance of the data.
     *
     * @param slot The slot index.
     * @param stack The ItemStack object.
     * @param count The Item count inside the ItemStack.
     */
    public ItemData(int slot, @Nonnull Object stack, int count) {
        this.slot = slot;
        this.stack = stack;
        this.count = count;
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

    /**
     * @return Gets the item count.
     */
    public int getCount() {
        return count;
    }
}
