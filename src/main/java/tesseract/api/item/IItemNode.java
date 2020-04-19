package tesseract.api.item;

import tesseract.graph.IConnectable;
import tesseract.graph.ITickHost;
import tesseract.util.Dir;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An item node is the unit of interaction with item inventories.
 * <p>
 * A reference implementation can be found at {@link net.minecraftforge.items.IItemHandler}.
 *
 * This interface represents a Item Storage. IT IS NOT REQUIRED but is provided for convenience.
 * You are free to handle Items in any way that you wish - this is simply an easy default way.
 * DO NOT ASSUME that these objects are used internally in all cases.
 */
public interface IItemNode extends IConnectable, ITickHost {

    /**
     * Inserts an ItemStack into an available slot and return the remainder.
     * @param stack ItemStack to insert. This must not be modified by the item handler.
     * @param simulate If true, the insertion is only simulated
     * @return The remaining ItemStack that was not inserted (if the entire stack is accepted, then return an empty ItemStack).
     *         May be the same as the input ItemStack if unchanged, otherwise a new ItemStack.
     *         The returned ItemStack can be safely modified after.
     **/
    int insert(@Nonnull Object stack, boolean simulate);

    /**
     * Extracts an ItemStack from an available slot.
     * @param slot The slot to extract from.
     * @param amount Amount to extract (may be greater than the current stack's max limit)
     * @param simulate If true, the extraction is only simulated
     * @return ItemStack extracted from the slot, must be null if nothing can be extracted.
     *         The returned ItemStack can be safely modified after, so item handlers should return a new or copied stack.
     **/
    @Nullable
    ItemData extract(int slot, int amount, boolean simulate);

    /**
     * @return Gets all available slots.
     **/
    @Nonnull
    int[] getAvailableSlots();

    /**
     * @param stack ItemStack holding the Item to be queried.
     * @return If the storage can hold the item (EVER, not at the time of query).
     */
    boolean canAccept(@Nonnull Object stack);

    /**
     * @return Gets the initial amount of items that can be output.
     */
    int getOutputAmount();

    /**
     * Gets if this storage can have item extracted.
     * @return If this is false, then any calls to extractEnergy will return 0.
     */
    boolean canOutput();

    /**
     * Used to determine if this storage can receive item.
     * @return If this is false, then any calls to receiveEnergy will return 0.
     */
    boolean canInput();

    /**
     * Used to determine which sides can output item (if any).
     * Output cannot be used as input.
     * @param direction Direction to the out.
     * @return Returns true if the given direction is output side.
     */
    boolean canOutput(@Nonnull Dir direction);
}
