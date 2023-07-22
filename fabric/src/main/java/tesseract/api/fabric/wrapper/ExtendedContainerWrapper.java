package tesseract.api.fabric.wrapper;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;
import tesseract.api.item.ExtendedItemContainer;

import java.util.Iterator;
import java.util.stream.IntStream;

public record ExtendedContainerWrapper(ExtendedItemContainer container) implements Storage<ItemVariant> {
    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        ItemStack stackToInsert = resource.toStack((int) maxAmount);
        ItemStack copyToInsert = stackToInsert.copy();
        for (int i = 0; i < container.getContainerSize(); i++) {
            copyToInsert = container.insertItem(i, copyToInsert, true);
            if (copyToInsert.isEmpty()) break;
        }
        transaction.addCloseCallback((t, r) -> {
            if (r.wasCommitted()){
                ItemStack copy = stackToInsert.copy();
                for (int i = 0; i < container.getContainerSize(); i++) {
                    copy = container.insertItem(i, copy, false);
                    if (copy.isEmpty()) break;
                }
            }
        });
        return maxAmount - copyToInsert.getCount();
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        int toExtract = (int) maxAmount;
        for (int i = 0; i < container.getContainerSize(); i++) {
            if (toExtract <= 0) break;
            ItemStack extract = container.extractItem(i, toExtract, true);
            if (extract.isEmpty() || !resource.matches(extract)) continue;
            toExtract -= extract.getCount();
        }
        transaction.addCloseCallback((transaction1, result) -> {
            int toExtractAmount = (int) maxAmount;
            for (int i = 0; i < container.getContainerSize(); i++) {
                if (toExtractAmount <= 0) break;
                ItemStack extract = container.extractItem(i, toExtractAmount, true);
                if (extract.isEmpty() || !resource.matches(extract)) continue;
                container.extractItem(i, toExtractAmount, false);
                toExtractAmount -= extract.getCount();
            }
        });
        return maxAmount - toExtract;
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        return(Iterator<StorageView<ItemVariant>>) (Object) IntStream.range(0, container.getContainerSize()).mapToObj(i -> container.getItem(i)).map(StackStorageView::new).iterator();
    }

    record StackStorageView(ItemStack stack) implements StorageView<ItemVariant>{

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            if (!resource.matches(stack)) return 0;
            int maxExtract = Math.min(stack.getCount(), (int) maxAmount);
            transaction.addCloseCallback((transaction1, result) -> {
                if (result.wasCommitted()) stack.shrink(maxExtract);
            });
            return maxExtract;
        }

        @Override
        public boolean isResourceBlank() {
            return stack.isEmpty();
        }

        @Override
        public ItemVariant getResource() {
            return ItemVariant.of(stack);
        }

        @Override
        public long getAmount() {
            return stack.getCount();
        }

        @Override
        public long getCapacity() {
            return stack.getMaxStackSize();
        }
    }
}
