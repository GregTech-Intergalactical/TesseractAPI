package tesseract.api.fabric.wrapper;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import tesseract.api.item.IItemNode;
import tesseract.util.ItemHandlerUtils;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class ItemStorageWrapper implements IItemNode {
    protected final Storage<ItemVariant> storage;
    protected long version;
    protected int slots;
    protected ItemStack[] stacks;
    protected Long[] capacities;

    public ItemStorageWrapper(@NotNull Storage<ItemVariant> storage){
        this.storage = storage;
        this.version = storage.getVersion();
        updateContents();
    }

    public Storage<ItemVariant> getStorage() {
        return storage;
    }

    public boolean shouldUpdate() {
        return storage.getVersion() != version;
    }

    private void updateContents() {
        List<ItemStack> stacks = new ArrayList<>();
        List<Long> capacities = new ArrayList<>();
        try (Transaction t = Transaction.openOuter()) {
            for (StorageView<ItemVariant> view : storage.iterable(t)) {
                stacks.add(view.getResource().toStack((int) view.getAmount()));
                capacities.add(view.getCapacity());
            }
            t.abort();
        }
        this.stacks = stacks.toArray(ItemStack[]::new);
        this.capacities = capacities.toArray(Long[]::new);
        this.slots = stacks.size();
        this.version = storage.getVersion();
    }

    private boolean validIndex(int slot) {
        return slot >= 0 && slot < slots;
    }

    @Override
    public int getContainerSize() {
        if (shouldUpdate())
            updateContents();
        return slots;
    }

    @NotNull
    @Override
    public ItemStack getItem(int slot) {
        if (validIndex(slot)) {
            if (shouldUpdate())
                updateContents();
            return stacks[slot].copy();
        }
        return ItemStack.EMPTY;
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean sim) {
        if (!validIndex(slot)) // first check valid slot index
            return stack;
        if (stack.isEmpty()) // check stack is not empty
            return stack;
        if (!canPlaceItem(slot, stack)) // make sure this stack can be stored
            return stack;
        if (!storage.supportsInsertion()) // make sure insertion is supported
            return stack;
        ItemStack current = getItem(slot);
        int limit = Math.min(getSlotLimit(slot), current.getMaxStackSize());
        if (limit <= 0 || !ItemHandlerUtils.canItemStacksStack(current, stack)) // make sure there's room
            return stack;
        // finally insert
        ItemStack finalVal = ItemStack.EMPTY;
        try (Transaction t = Transaction.openOuter()) {
            // this technically breaks spec and ignores 'slot' but thanks FAPI, we literally have no choice!
            long remainder = stack.getCount() - storage.insert(ItemVariant.of(stack), stack.getCount(), t);
            if (remainder != 0) {
                finalVal = new ItemStack(stack.getItem(), (int) remainder);
            }

            if (sim) t.abort();
            else {
                t.commit();
                if (shouldUpdate())
                    updateContents();
            }
        }
        return finalVal;
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean sim) {
        if (amount <= 0)
            return ItemStack.EMPTY;
        if (!validIndex(slot)) // check valid slot index
            return ItemStack.EMPTY;
        if (!storage.supportsExtraction()) // make sure insertion is supported
            return ItemStack.EMPTY;

        ItemStack finalVal = ItemStack.EMPTY;
        try (Transaction t = Transaction.openOuter()) {
            int index = 0;
            for (StorageView<ItemVariant> view : storage.iterable(t)) {
                if (index == slot) {
                    ItemVariant variant = view.getResource();
                    long extracted = view.isResourceBlank() ? 0 : view.extract(variant, amount, t);
                    if (extracted != 0) {
                        finalVal = variant.toStack((int) extracted);
                    }
                    break;
                }
                index++;
            }
            if (sim) t.abort();
            else {
                t.commit();
                if (shouldUpdate())
                    updateContents();
            }
        }
        return finalVal;
    }

    @Override
    public int getSlotLimit(int slot) {
        if (validIndex(slot)) {
            if (shouldUpdate())
                updateContents();
            return (int) (long) capacities[slot];
        }
        return 0;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return true;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        // jank
        extractItem(slot, getSlotLimit(slot), false);
        insertItem(slot, stack, false);
    }

    @Override
    public int getPriority(Direction direction) {
        return 0;
    }

    @Override
    public boolean isEmpty(int slot) {
        return getItem(slot).isEmpty();
    }

    @Override
    public boolean canOutput() {
        return storage.supportsExtraction();
    }

    @Override
    public boolean canInput() {
        return storage.supportsInsertion();
    }

    @Override
    public boolean canInput(Direction direction) {
        return canInput();
    }

    @Override
    public boolean canOutput(Direction direction) {
        return canOutput();
    }

    @Override
    public void deserialize(CompoundTag nbt) {

    }

    @Override
    public CompoundTag serialize(CompoundTag nbt) {
        return null;
    }
}
