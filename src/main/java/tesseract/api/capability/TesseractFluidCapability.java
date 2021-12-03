package tesseract.api.capability;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import tesseract.Tesseract;
import tesseract.api.fluid.*;
import tesseract.graph.Graph;
import tesseract.graph.Path;
import tesseract.util.Pos;

import javax.annotation.Nonnull;

public class TesseractFluidCapability<T extends TileEntity & IFluidPipe> extends TesseractBaseCapability<T> implements IFluidHandler {

    private FluidTransaction old;

    public TesseractFluidCapability(T tile, Direction dir, boolean isNode, ITransactionModifier callback) {
        super(tile, dir, isNode, callback);
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
        return true;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (this.isSending) return 0;
        this.isSending = true;
        if (action.execute()) {
            if (this.isNode) {
                for (FluidStack stac : this.old.getData()) {
                    callback.modify(stac, this.side, modifyDirs.pop(), false);
                }
            }
            old.commit();
        } else {
            long pos = tile.getBlockPos().asLong();
            FluidTransaction transaction = new FluidTransaction(resource.copy(), a -> {
            });
            if (!this.isNode) {
                Tesseract.FLUID.getController(tile.getLevel(), pos).insert(pos, side, transaction);
            } else {
                modifyDirs.clear();
                FluidStack current = resource.copy();
                for (Direction dir : Graph.DIRECTIONS) {
                    if (dir == this.side)
                        continue;
                    TileEntity tile = this.tile.getLevel().getBlockEntity(BlockPos.of(Pos.offset(pos, dir)));
                    if (tile == null)
                        continue;
                    LazyOptional<IFluidHandler> cap = tile
                            .getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir.getOpposite());
                    IFluidHandler handle = cap.orElse(null);
                    if (handle == null)
                        continue;
                    int inserted = handle.fill(current, action);
                    if (inserted > 0) {
                        // Amount actually inserted
                        FluidStack copy = current.copy();
                        copy.setAmount(inserted);
                        callback.modify(copy, this.side, dir, true);
                        current.setAmount(current.getAmount() - copy.getAmount());
                        modifyDirs.add(dir);
                        transaction.addData(copy, a -> {
                            FluidController c = ((FluidController)Tesseract.FLUID.getController(tile.getLevel(), tile.getBlockPos().asLong()));
                            c.dataCommit(new FluidConsumer(new IFluidNode.FluidTileWrapper(this.tile,handle), Path.of(tile.getBlockPos().asLong(), ((IFluidPipe) tile), this.side, dir), dir), a);
                        });
                    }
                }
            }
            this.old = transaction;
        }
        this.isSending = false;
        return resource.getAmount() - this.old.stack.getAmount();
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return FluidStack.EMPTY;
    }
}
