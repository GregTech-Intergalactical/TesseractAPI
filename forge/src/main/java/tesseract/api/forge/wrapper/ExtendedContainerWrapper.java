package tesseract.api.forge.wrapper;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import tesseract.api.item.ExtendedItemContainer;

public class ExtendedContainerWrapper extends InvWrapper {
    public ExtendedContainerWrapper(ExtendedItemContainer inv) {
        super(inv);
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return ((ExtendedItemContainer)getInv()).insertItem(slot, stack, simulate);
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ((ExtendedItemContainer)getInv()).extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return ((ExtendedItemContainer)getInv()).getSlotLimit(slot);
    }
}
