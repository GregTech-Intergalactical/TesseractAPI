package tesseract.api.forge.wrapper;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import tesseract.api.item.IItemNode;

public record ItemHandlerWrapper(IItemHandler handler) implements IItemNode {

    @Override
    public int getPriority(Direction direction) {
        return 0;
    }

    @Override
    public boolean isEmpty(int slot) {
        return handler.getStackInSlot(slot).isEmpty();
    }

    @Override
    public boolean canOutput() {
        return handler != null;
    }

    @Override
    public boolean canInput() {
        return handler != null;
    }

    @Override
    public boolean canInput(Direction direction) {
        return handler != null;
    }

    @Override
    public boolean canOutput(Direction direction) {
        return handler != null;
    }

    @Override
    public int getContainerSize() {
        return handler.getSlots();
    }

    @NotNull
    @Override
    public ItemStack getItem(int slot) {
        return handler.getStackInSlot(slot);
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        if (handler instanceof IItemHandlerModifiable modifiable) modifiable.setStackInSlot(index, stack);
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return handler.insertItem(slot, stack, simulate);
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return handler.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return handler.getSlotLimit(slot);
    }

    @Override
    public boolean canPlaceItem(int slot, @NotNull ItemStack stack) {
        return handler.isItemValid(slot, stack);
    }

    @Override
    public void deserialize(CompoundTag nbt) {
        
    }

    @Override
    public CompoundTag serialize(CompoundTag nbt) {
        return null;
    }
}
