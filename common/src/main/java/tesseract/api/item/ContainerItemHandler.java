package tesseract.api.item;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface ContainerItemHandler extends PlatformItemHandler {
    ExtendedItemContainer getContainer();

    @Override
    default void setStackInSlot(int slot, @NotNull ItemStack stack){
        getContainer().setItem(slot, stack);
    }

    @Override
    default @NotNull ItemStack getStackInSlot(int slot){
        return getContainer().getItem(slot);
    }

    @Override
    default int getSlots(){
        return getContainer().getContainerSize();
    }

    @Override
    default @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate){
        return getContainer().insertItem(slot, stack, simulate);
    }

    @Override
    default @NotNull ItemStack extractItem(int slot, @NotNull int amount, boolean simulate){
        return getContainer().extractItem(slot, amount, simulate);
    }

    @Override
    default int getSlotLimit(int slot){
        return getContainer().getSlotLimit(slot);
    }

    @Override
    default boolean isItemValid(int slot, @NotNull ItemStack stack){
        return getContainer().canPlaceItem(slot, stack);
    }
}
