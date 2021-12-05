package tesseract.api.capability;

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
import tesseract.api.item.ItemConsumer;
import tesseract.api.item.ItemController;
import tesseract.api.item.ItemTransaction;
import tesseract.graph.Graph;
import tesseract.graph.Path;
import tesseract.util.Pos;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nonnull;


public class TesseractItemCapability<T extends TileEntity & IItemPipe> extends TesseractBaseCapability<T> implements IItemHandler {
    
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
            modifyDirs.clear();
            ItemTransaction transaction = new ItemTransaction(stack, a -> {
            });
            long pos = tile.getBlockPos().asLong();
            if (!isNode) {
                Tesseract.ITEM.getController(tile.getLevel(), pos).insert(pos, this.side, transaction);
            } else {
                ItemStack current = stack.copy();
                for (Direction dir : Graph.DIRECTIONS) {
                    if (dir == this.side) continue;
                    if (!this.tile.connects(dir)) continue;
                    TileEntity tile = this.tile.getLevel().getBlockEntity(BlockPos.of(Pos.offset(pos, dir)));
                    if (tile == null) continue;
                    LazyOptional<IItemHandler> cap = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.getOpposite());
                    IItemHandler handle = cap.orElse(null);
                    if (handle == null) continue;
                    int used = 0;
                    for (int i = 0; i < handle.getSlots() && (this.tile.getCapacity() - this.tile.getHolder() - used) > 0; i++) {
                        ItemStack inserted = handle.insertItem(i, current, true);
                        if (inserted.getCount() < current.getCount()) {
                            used++;
                            //Amount actually inserted
                            int count = current.getCount() - inserted.getCount();
                            inserted = stack.copy();
                            inserted.setCount(count);
                            callback.modify(inserted, this.side, dir, true);
                            count = current.getCount() - inserted.getCount();
                            current.setCount(count);
                            final int ii = i;
                            modifyDirs.add(dir);
                            transaction.addData(inserted, a -> {
                                for (ItemStack stac : this.old.getData()) {
                                    callback.modify(stac, this.side, modifyDirs.pop(), false);
                                }
                                //ItemController has no extra method over transfer counting
                                //ItemController c = ((ItemController)Tesseract.ITEM.getController(tile.getLevel(), tile.getBlockPos().asLong()));
                                //c.dataCommit(new ItemConsumer(new IItemNode.ItemTileWrapper(this.tile,handle), Path.of(tile.getBlockPos().asLong(), ((IItemPipe) this.tile), this.side, dir), dir), a, a.getCount());
                                handle.insertItem(ii, a, false);
                            });
                        }
                    }
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
}
