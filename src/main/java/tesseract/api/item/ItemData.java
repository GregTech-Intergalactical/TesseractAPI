package tesseract.api.item;

import javax.annotation.Nonnull;

/**
 * A class that acts as a wrapper for a item stack and a slot index.
 */
public class ItemData {

    private final int slot;
    private final Object stack;
    private final Object item;

    /**
     * Creates instance of the data.
     *
     * @param slot The slot index.
     * @param stack The ItemStack object.
     * @param item The item inside an ItemStack.
     */
    public ItemData(int slot, @Nonnull Object stack, @Nonnull Object item) {
        this.slot = slot;
        this.stack = stack;
        this.item = item;
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
     * @return Gets the fluid inside a FluidStack.
     */
    @Nonnull
    public Object getItem() {
        return item;
    }
}
