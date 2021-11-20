package tesseract.api.capability;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.items.IItemHandler;
import tesseract.Tesseract;
import tesseract.api.item.ItemTransaction;
import tesseract.graph.Graph;
import tesseract.util.Pos;

import javax.annotation.Nonnull;

public class TesseractItemCapability implements IItemHandler {
    //The pipe.
    TileEntity tile;
    Direction side;

    ItemTransaction old;

    public TesseractItemCapability(TileEntity tile, Direction dir) {
        this.tile = tile;
        this.side = dir;
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
            long pos = tile.getBlockPos().asLong();
            ItemTransaction transaction = new ItemTransaction(stack, a -> {
            });
            Tesseract.ITEM.getController(tile.getLevel(), pos).insert(side == null ? pos : Pos.offset(pos, Graph.DIRECTIONS[side.get3DDataValue()]), pos, transaction);
            this.old = transaction;
        }
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
