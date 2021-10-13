package tesseract.api.item;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;


/**
 * An item node is the unit of interaction with item inventories.
 * <p>
 * This interface represents an Item Storage. IT IS NOT REQUIRED but is provided for convenience.
 * You are free to handle Items in any way that you wish - this is simply an easy default way.
 * DO NOT ASSUME that these objects are used internally in all cases.
 * </p>
 */
public interface IItemNode extends IItemHandler {

    /**
     * @param direction Direction to the proceed.
     * @return Returns the priority of this node as a number.
     */
    int getPriority(Direction direction);

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
    boolean canInput(Direction direction);

    /**
     * Used to determine which sides can output item (if any).
     * @param direction Direction to the output.
     * @return Returns true if the given direction is output side.
     */
    boolean canOutput(Direction direction);

    /**
     * Used to determine which items and at which direction can be consumed.
     * @param item The Item to be queried.
     * @param direction Direction to the input.
     * @return If the storage can input the item (EVER, not at the time of query).
     */
    default boolean canInput(ItemStack item, Direction direction) {
        return true;
    }

    static IItemNode fromPipe(IItemPipe pipe) {
        return new IItemNode() {
            @Override
            public int getPriority(Direction direction) {
                return 0;
            }

            @Override
            public boolean isEmpty(int slot) {
                return false;
            }

            @Override
            public boolean canOutput() {
                return true;
            }

            @Override
            public boolean canInput() {
                return false;
            }

            @Override
            public boolean canInput(Direction direction) {
                return false;
            }

            @Override
            public boolean canOutput(Direction direction) {
                return pipe.connects(direction);
            }

            @Override
            public int getSlots() {
                return 0;
            }

            @Nonnull
            @Override
            public ItemStack getStackInSlot(int slot) {
                return ItemStack.EMPTY;
            }

            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                return stack;
            }

            @Nonnull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return ItemStack.EMPTY;
            }

            @Override
            public int getSlotLimit(int slot) {
                return 0;
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return false;
            }
        };
    }
}
