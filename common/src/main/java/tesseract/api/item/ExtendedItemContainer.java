package tesseract.api.item;


import earth.terrarium.botarium.common.item.SerializableContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface ExtendedItemContainer extends SerializableContainer, ContainerItemHandler {

    @Override
    default ExtendedItemContainer getContainer(){
        return this;
    }

    /**
     * <p>
     * Inserts an ItemStack into the given slot and return the remainder.
     * The ItemStack <em>should not</em> be modified in this function!
     * </p>
     *
     * @param slot     Slot to insert into.
     * @param stack    ItemStack to insert. This must not be modified by the item handler.
     * @param simulate If true, the insertion is only simulated
     * @return The remaining ItemStack that was not inserted (if the entire stack is accepted, then return an empty ItemStack).
     *         May be the same as the input ItemStack if unchanged, otherwise a new ItemStack.
     *         The returned ItemStack can be safely modified after.
     **/
    @NotNull
    ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate);

    /**
     * Extracts an ItemStack from the given slot.
     * <p>
     * The returned value must be empty if nothing is extracted,
     * otherwise its stack size must be less than or equal to {@code amount} and {@link ItemStack#getMaxStackSize()}.
     * </p>
     *
     * @param slot     Slot to extract from.
     * @param amount   Amount to extract (may be greater than the current stack's max limit)
     * @param simulate If true, the extraction is only simulated
     * @return ItemStack extracted from the slot, must be empty if nothing can be extracted.
     *         The returned ItemStack can be safely modified after, so item handlers should return a new or copied stack.
     **/
    @NotNull
    ItemStack extractItem(int slot, int amount, boolean simulate);

    /**
     * Retrieves the maximum stack size allowed to exist in the given slot.
     *
     * @param slot Slot to query.
     * @return     The maximum stack size allowed in the slot.
     */
    int getSlotLimit(int slot);

    //Container overrides

    @Override
    default ItemStack removeItem(int index, int count){
        return extractItem(index, count, false);
    }

    @Override
    default ItemStack removeItemNoUpdate(int index) {
        return removeItem(index, getItem(index).getCount());
    }

    @Override
    default boolean isEmpty(){
        boolean hasStack = false;
        for (int i = 0; i < getContainerSize(); i++) {
            ItemStack stack = getItem(i);
            if (!stack.isEmpty()) hasStack = true;
        };
        return !hasStack;
    }

    @Override
    default boolean stillValid(Player player){
        return true;
    }

    @Override
    default void setChanged(){

    }

    @Override
    default void clearContent(){
        for (int i = 0; i < getContainerSize(); i++) {
            this.removeItem(i, getItem(i).getCount());
        }
    }
}
