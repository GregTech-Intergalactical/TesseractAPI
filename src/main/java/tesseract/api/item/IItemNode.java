package tesseract.api.item;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
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
public interface IItemNode extends IItemHandler, IRefreshable {

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
     * Used to determine if this storage can receive item.
     * @return If this is false, then any calls to receiveEnergy will return 0.
     */
    boolean canInput(Dir direction);

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
    default boolean canInput(ItemStack item, Dir direction) {
        return true;
    }
}
