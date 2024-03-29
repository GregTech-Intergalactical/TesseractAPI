package tesseract.api.item;

import net.minecraft.world.item.ItemStack;
import tesseract.api.Transaction;

import java.util.function.Consumer;

public class ItemTransaction extends Transaction<ItemStack> {

    public final ItemStack stack;

    public ItemTransaction(ItemStack stack, Consumer<ItemStack> consumer) {
        super(consumer);
        this.stack = stack.copy();
    }

    @Override
    public boolean isValid() {
        return !stack.isEmpty();
    }

    public void addData(ItemStack count, Consumer<ItemStack> consumer) {
        this.stack.setCount(this.stack.getCount() - count.getCount());
        this.addData(count);
        this.onCommit(consumer);
    }

    public void addData(int count, Consumer<ItemStack> consumer) {
        var st = this.stack.copy();
        st.setCount(count);
        addData(st, consumer);
    }

    @Override
    public boolean canContinue() {
        return !stack.isEmpty();
    }
}
