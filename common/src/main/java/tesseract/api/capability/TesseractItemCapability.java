package tesseract.api.capability;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import tesseract.Tesseract;
import tesseract.api.item.IItemNode;
import tesseract.api.item.IItemPipe;
import tesseract.api.item.ItemTransaction;
import tesseract.graph.Graph;
import tesseract.util.Pos;


public class TesseractItemCapability<T extends BlockEntity & IItemPipe> extends TesseractBaseCapability<T> implements IItemNode {
    
    private ItemTransaction old;
    
    public TesseractItemCapability(T tile, Direction dir, boolean isNode, ITransactionModifier onTransaction) {
        super(tile, dir, isNode, onTransaction);
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return ItemStack.EMPTY;
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (!simulate) {
            old.commit();
        } else {
            if (this.isSending) return stack;
            this.isSending = true;
            ItemTransaction transaction = new ItemTransaction(stack, a -> {});
            long pos = tile.getBlockPos().asLong();
            if (!isNode) {
                Tesseract.ITEM.getController(tile.getLevel(), pos).insert(pos, this.side, transaction, callback);
            } else {
                for (Direction dir : Graph.DIRECTIONS) {
                    if (dir == this.side || !this.tile.connects(dir)) continue;
                    Tesseract.ITEM.getController(tile.getLevel(), pos).insert(Pos.offset(pos, dir), dir.getOpposite(), transaction, callback);
                }
            }
            this.old = transaction;
        }
        this.isSending = false;
        return old.stack.copy();
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return true;
    }

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
        return true;
    }

    @Override
    public boolean canInput(Direction direction) {
        return true;
    }

    @Override
    public boolean canOutput(Direction direction) {
        return true;
    }
}
