package tesseract.api.capability;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import tesseract.Tesseract;
import tesseract.api.item.IItemNode;
import tesseract.api.item.IItemPipe;
import tesseract.api.item.ItemTransaction;
import tesseract.graph.Graph;
import tesseract.util.Pos;


public class TesseractItemCapability<T extends TileEntity & IItemPipe> extends TesseractBaseCapability<T> implements IItemNode {
    
    private ItemTransaction old;
    
    public TesseractItemCapability(T tile, Direction dir, boolean isNode, ITransactionModifier onTransaction) {
        super(tile, dir, isNode, onTransaction);
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
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

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
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
