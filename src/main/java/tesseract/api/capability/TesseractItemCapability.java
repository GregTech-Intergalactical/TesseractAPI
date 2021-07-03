package tesseract.api.capability;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.items.IItemHandler;
import tesseract.Tesseract;
import tesseract.graph.Graph;
import tesseract.util.Pos;

import javax.annotation.Nonnull;

public class TesseractItemCapability implements IItemHandler {
    //The pipe.
    TileEntity tile;
    Direction side;

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
        int inserted = Tesseract.ITEM.getController(tile.getWorld(), tile.getPos().toLong()).insert(new Pos(tile.getPos().toLong()), Graph.DIRECTIONS[side.getIndex()],stack, simulate);
        ItemStack newStack = stack.copy();
        newStack.setCount(inserted);
        return newStack;
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
