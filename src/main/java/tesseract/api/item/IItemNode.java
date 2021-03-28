package tesseract.api.item;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.item.ItemStack;
import tesseract.api.IConnectable;
import tesseract.api.IRefreshable;
import tesseract.util.Dir;

/**
 * An item node is the unit of interaction with item inventories.
 * <p>
 * This interface represents an Item Storage. IT IS NOT REQUIRED but is provided for convenience.
 * You are free to handle Items in any way that you wish - this is simply an easy default way.
 * DO NOT ASSUME that these objects are used internally in all cases.
 * </p>
 */
public interface IItemNode extends IConnectable, IRefreshable {

    /**
     * Inserts an item into an available slot and return the remainder.
     * @param data ItemData to insert. This must not be modified by the item handler.
     * @param simulate If true, the insertion is only simulated
     * @return The remaining ItemData that was not inserted (if the entire stack is accepted, then return an empty ItemData).
     *         May be the same as the input ItemData if unchanged, otherwise a new ItemData.
     *         The returned ItemData can be safely modified after.
     **/
    int insert(ItemStack data, boolean simulate);

    /**
     * Extracts an item from an available slot.
     * @param slot The slot to extract from.
     * @param amount Amount to extract (may be greater than the current stack's max limit)
     * @param simulate If true, the extraction is only simulated
     * @return ItemData extracted from the slot, must be null if nothing can be extracted.
     *         The returned ItemData can be safely modified after, so item handlers should return a new or copied stack.
     **/
    ItemStack extract(int slot, int amount, boolean simulate);

    /**
     * @param direction The direction index.
     * @return Gets all available slots.
     **/
    IntList getAvailableSlots(Dir direction);

    /**
     * @param direction Direction to the proceed.
     * @return Gets the initial amount of items that can be output.
     */
    int getOutputAmount(Dir direction);

    /**
     * @param direction Direction to the proceed.
     * @return Returns the priority of this node as a number.
     */
    int getPriority(Dir direction);

    /**
     * @param slot The slot index.
     * @return It returns True if the slot has no items else it returns false.
     */
    boolean isEmpty(int slot);

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
     * @param direction Direction to the output.
     * @return Returns true if the given direction is output side.
     */
    boolean canOutput(Dir direction);

    /**
     * Used to determine which items and at which direction can be consumed.
     * @param item The Item to be queried.
     * @param direction Direction to the input.
     * @return If the storage can input the item (EVER, not at the time of query).
     */
    boolean canInput(ItemStack item, Dir direction);
}
