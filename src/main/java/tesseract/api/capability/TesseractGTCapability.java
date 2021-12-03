package tesseract.api.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.LazyOptional;
import tesseract.Tesseract;
import tesseract.api.fluid.FluidConsumer;
import tesseract.api.fluid.FluidController;
import tesseract.api.fluid.IFluidNode;
import tesseract.api.gt.*;
import tesseract.graph.Graph;
import tesseract.graph.Path;
import tesseract.util.Pos;

import javax.annotation.Nullable;

public class TesseractGTCapability<T extends TileEntity & IGTCable> extends TesseractBaseCapability<T> implements IEnergyHandler {
    @CapabilityInject(IEnergyHandler.class)
    public static final Capability<IEnergyHandler> ENERGY_HANDLER_CAPABILITY;

    static {
        ENERGY_HANDLER_CAPABILITY = null;
    }

    public static void register() {
        CapabilityManager.INSTANCE.register(IEnergyHandler.class, new Capability.IStorage<IEnergyHandler>() {
            @Nullable
            @Override
            public INBT writeNBT(Capability<IEnergyHandler> capability, IEnergyHandler instance, Direction side) {
                return null;
            }

            @Override
            public void readNBT(Capability<IEnergyHandler> capability, IEnergyHandler instance, Direction side, INBT nbt) {

            }
        }, () -> new IEnergyHandler() {
            @Override
            public GTConsumer.State getState() {
                return null;
            }

            @Override
            public CompoundNBT serializeNBT() {
                return new CompoundNBT();
            }

            @Override
            public void deserializeNBT(CompoundNBT nbt) {

            }

            @Override
            public boolean insert(GTTransaction transaction) {
                return false;
            }

            @Override
            public boolean extractEnergy(GTTransaction.TransferData data) {
                return false;
            }

            @Override
            public boolean addEnergy(GTTransaction.TransferData data) {
                return false;
            }

            @Override
            public GTTransaction extract(GTTransaction.Mode mode) {
                return new GTTransaction(0, 0, a -> {
                });
            }

            @Override
            public long getEnergy() {
                return 0;
            }

            @Override
            public long getCapacity() {
                return 0;
            }

            @Override
            public long getOutputAmperage() {
                return 0;
            }

            @Override
            public long getOutputVoltage() {
                return 0;
            }

            @Override
            public long getInputAmperage() {
                return 0;
            }

            @Override
            public long getInputVoltage() {
                return 0;
            }

            @Override
            public boolean canOutput() {
                return false;
            }

            @Override
            public boolean canInput() {
                return false;
            }

            @Override
            public boolean canInput(Direction direction) {
                return false;
            }

            @Override
            public boolean canOutput(Direction direction) {
                return false;
            }
        });
    }

    private final IGTCable cable;
    private long holder;

    public TesseractGTCapability(T tile, Direction dir, boolean isNode, ITransactionModifier modifier) {
        super(tile, dir, isNode, modifier);
        this.cable = (IGTCable) tile;
        holder = GTHolder.create(cable, 0);
    }

    @Override
    public boolean insert(GTTransaction transaction) {
        boolean flag = false;
        if (this.isSending) return false;
        this.isSending = true;
        long pos = tile.getBlockPos().asLong();
        if (!this.isNode) {
            long old = transaction.getAvailableAmps();
            Tesseract.GT_ENERGY.getController(tile.getLevel(), pos).insert(pos, side, transaction);
            flag = transaction.getAvailableAmps() < old;
        } else {
            modifyDirs.clear();
            for (Direction dir : Graph.DIRECTIONS) {
                if (dir == this.side)
                    continue;
                if (!this.tile.connects(dir)) continue;
                TileEntity tile = this.tile.getLevel().getBlockEntity(BlockPos.of(Pos.offset(pos, dir)));
                if (tile == null)
                    continue;
                LazyOptional<IEnergyHandler> cap = tile
                        .getCapability(ENERGY_HANDLER_CAPABILITY, dir.getOpposite());
                IEnergyHandler handle = cap.orElse(null);
                if (handle == null)
                    continue;
                if (handle.insert(transaction)) {
                    flag = true;
                    this.callback.modify(transaction.getLast(), this.side, dir, true);
                    GTController c = ((GTController)Tesseract.GT_ENERGY.getController(tile.getLevel(), tile.getBlockPos().asLong()));
                    transaction.pushCallback(a -> {
                        callback.modify(a, this.side, modifyDirs.pop(), false);
                        c.dataCommit(new GTConsumer(handle, Path.of(this.tile.getBlockPos().asLong(), cable, this.side, dir)), a);
                    });
                    modifyDirs.add(dir);
                }
                if (!transaction.canContinue()) break;
            }
        }
        this.isSending = false;
        return flag;
    }

    @Override
    public boolean extractEnergy(GTTransaction.TransferData data) {
        return false;
    }

    @Override
    public boolean addEnergy(GTTransaction.TransferData data) {
        return false;
    }

    @Override
    public GTTransaction extract(GTTransaction.Mode mode) {
        return new GTTransaction(0, 0, a -> {
        });
    }

    @Override
    public long getEnergy() {
        return 0;
    }

    @Override
    public long getCapacity() {
        return 0;
    }

    @Override
    public long getOutputAmperage() {
        return Long.MAX_VALUE;
    }

    @Override
    public long getOutputVoltage() {
        return cable.getVoltage();
    }

    @Override
    public long getInputAmperage() {
        return Long.MAX_VALUE;
    }

    @Override
    public long getInputVoltage() {
        return cable.getVoltage();
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

    @Override
    public CompoundNBT serializeNBT() {
        return new CompoundNBT();
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {

    }

    @Override
    public GTConsumer.State getState() {
        return new GTConsumer.State(this);
    }
}
