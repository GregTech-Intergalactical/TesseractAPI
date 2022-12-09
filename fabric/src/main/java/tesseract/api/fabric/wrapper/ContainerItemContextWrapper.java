package tesseract.api.fabric.wrapper;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import tesseract.api.context.TesseractItemContext;

public class ContainerItemContextWrapper implements TesseractItemContext {
    private final ContainerItemContext context;
    public ContainerItemContextWrapper(ContainerItemContext context){
        this.context = context;

    }

    @Override
    public @NotNull CompoundTag getTag() {
        if (context.getItemVariant().isBlank()) return new CompoundTag();
        CompoundTag tag = context.getItemVariant().getNbt();
        if (tag == null){
            ItemStack stack = context.getItemVariant().toStack((int) context.getAmount());
            stack.setTag(new CompoundTag());
            setItemStack(stack);
        }
        return context.getItemVariant().getNbt();
    }

    @Override
    public Item getItem() {
        return context.getItemVariant().getItem();
    }

    @Override
    public int getCount() {
        return (int) context.getAmount();
    }

    @Override
    public void setItemStack(ItemStack stack) {
        Transaction transaction = Transaction.openOuter();
        context.exchange(ItemVariant.of(stack), stack.getCount(), transaction);
        transaction.commit();
    }
}
