package tesseract.api.wrapper;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import tesseract.api.item.ExtendedItemContainer;
import tesseract.api.item.IItemNode;
import tesseract.util.ItemHandlerUtils;

import javax.annotation.Nonnull;

public record ItemTileWrapper(BlockEntity tile,
                              Container handler) implements IItemNode {

    @Override
    public int getPriority(Direction direction) {
        return 0;
    }

    @Override
    public boolean isEmpty(int slot) {
        return handler.getItem(slot).isEmpty();
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
        return true;
    }

    @Override
    public int getContainerSize() {
        return handler.getContainerSize();
    }

    @Override
    public boolean isEmpty() {
        return handler.isEmpty();
    }

    @NotNull
    @Override
    public ItemStack getItem(int slot) {
        return handler.getItem(slot);
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return handler.removeItemNoUpdate(index);
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        handler.setItem(index, stack);
    }

    @Override
    public void setChanged() {
        handler.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return handler.stillValid(player);
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (handler instanceof ExtendedItemContainer extendedItemContainer) return extendedItemContainer.insertItem(slot, stack, simulate);
        if (stack.isEmpty())
            return ItemStack.EMPTY;
        if (!canPlaceItem(slot, stack))
            return stack;
        ItemStack existing = this.handler.getItem(slot);
        int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());;
        if (!existing.isEmpty()) {
            if (!ItemHandlerUtils.canItemStacksStack(stack, existing))
                return stack;
            limit -= existing.getCount();
        }
        if (limit <= 0)
            return stack;

        boolean reachedLimit = stack.getCount() > limit;
        if (!simulate) {
            if (existing.isEmpty()) {
                this.handler.setItem(slot, reachedLimit ? ItemHandlerUtils.copyStackWithSize(stack, limit) : stack);
            } else {
                existing.grow(reachedLimit ? limit : stack.getCount());
            }
        }
        return reachedLimit ? ItemHandlerUtils.copyStackWithSize(stack, stack.getCount()- limit) : ItemStack.EMPTY;
    }

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (handler instanceof ExtendedItemContainer extendedItemContainer) return extendedItemContainer.extractItem(slot, amount, simulate);
        if (amount == 0)
            return ItemStack.EMPTY;
        ItemStack existing = this.handler.getItem(slot);

        if (existing.isEmpty())
            return ItemStack.EMPTY;
        int toExtract = Math.min(amount, existing.getMaxStackSize());
        if (existing.getCount() <= toExtract) {
            if (!simulate) {
                this.handler.setItem(slot, ItemStack.EMPTY);
                return existing;
            } else {
                return existing.copy();
            }
        } else {
            if (!simulate) {
                this.handler.setItem(slot, ItemHandlerUtils.copyStackWithSize(existing, existing.getCount() - toExtract));
            }
            return ItemHandlerUtils.copyStackWithSize(existing, toExtract);
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        if (handler instanceof ExtendedItemContainer extendedItemContainer) return extendedItemContainer.getSlotLimit(slot);
        return handler.getMaxStackSize();
    }

    @Override
    public boolean canPlaceItem(int slot, @NotNull ItemStack stack) {
        return handler.canPlaceItem(slot, stack);
    }

    @Override
    public void deserialize(CompoundTag nbt) {
        
    }

    @Override
    public CompoundTag serialize(CompoundTag nbt) {
        return null;
    }

    @Override
    public void clearContent() {
        handler.clearContent();
    }
}
