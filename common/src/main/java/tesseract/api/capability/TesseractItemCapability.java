package tesseract.api.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import tesseract.TesseractCapUtils;
import tesseract.TesseractGraphWrappers;
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
    public ItemStack insertItem(int slot, @NotNull ItemStack stackIn, boolean simulate) {
        if (!simulate) {
            old.commit();
        } else {
            if (this.isSending) {
                return stackIn;
            }
            this.isSending = true;
            ItemTransaction transaction = new ItemTransaction(stackIn, a -> {});
            long pos = tile.getBlockPos().asLong();
            if (!isNode) {
                TesseractGraphWrappers.ITEM.getController(tile.getLevel(), pos).insert(pos, this.side, transaction, callback);
            } else {
                transferAroundPipe(transaction, pos);
            }
            this.old = transaction;
        }
        this.isSending = false;
        return old.stack.copy();
    }

    private void transferAroundPipe(ItemTransaction transaction, long pos) {
        ItemStack stackIn = transaction.stack.copy();
        for (Direction dir : Graph.DIRECTIONS) {
            if (dir == this.side || !this.tile.connects(dir)) continue;
            ItemStack stack = stackIn.copy();
            //First, perform cover modifications.
            this.callback.modify(stack, this.side, dir, true);
            if (stack.isEmpty()) continue;
            BlockEntity otherTile = tile.getLevel().getBlockEntity(BlockPos.of(Pos.offset(pos, dir)));
            if (otherTile != null) {
                //Check the handler.
                var cap = TesseractCapUtils.getItemHandler(otherTile, dir.getOpposite());
                if (cap.isEmpty()) continue;
                //Perform insertion, and add to the transaction.
                var handler = cap.get();
                var newStack = ItemHandlerHelper.insertItem(handler, stack, true);
                if (newStack.getCount() < stack.getCount()) {
                    transaction.addData(stack.getCount() - newStack.getCount(), a -> {
                        this.callback.modify(a, this.side, dir, false);
                        ItemHandlerHelper.insertItem(handler, a, false);
                    });
                    stackIn = newStack;
                }
                if (stackIn.isEmpty()) break;
            }
        }
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
