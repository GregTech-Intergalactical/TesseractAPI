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
import tesseract.api.gt.GTConsumer;
import tesseract.api.gt.GTTransaction;
import tesseract.api.gt.IEnergyHandler;
import tesseract.api.gt.IGTNode;
import tesseract.graph.Graph;
import tesseract.util.Pos;

import javax.annotation.Nullable;

public class TesseractGTCapability extends TesseractBaseCapability implements IEnergyHandler {
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

    public TesseractGTCapability(TileEntity tile, Direction dir, boolean isNode, ITransactionModifier modifier) {
        super(tile, dir, isNode, modifier);
    }

    @Override
    public boolean insert(GTTransaction transaction) {
        if (this.isSending) return false;
        this.isSending = true;
        long pos = tile.getBlockPos().asLong();
        if (!this.isNode) {
            Tesseract.GT_ENERGY.getController(tile.getLevel(), pos).insert(pos, side, transaction);
        } else {
            modifyDirs.clear();
            for (Direction dir : Graph.DIRECTIONS) {
                if (dir == this.side)
                    continue;
                TileEntity tile = this.tile.getLevel().getBlockEntity(BlockPos.of(Pos.offset(pos, dir)));
                if (tile == null)
                    continue;
                LazyOptional<IEnergyHandler> cap = tile
                        .getCapability(ENERGY_HANDLER_CAPABILITY, dir.getOpposite());
                IEnergyHandler handle = cap.orElse(null);
                if (handle == null)
                    continue;
                handle.insert(transaction);
                this.callback.modify(transaction.getLast(), this.side, dir, true);
                transaction.pushCallback(t -> {
                    callback.modify(t, this.side, modifyDirs.pop(), false);
                });
                modifyDirs.add(dir);
                if (!transaction.canContinue()) break;
            }
        }
        this.isSending = false;
        return transaction.isValid();
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
        return true;
    }

    @Override
    public boolean canInput(Direction direction) {
        return true;
    }

    @Override
    public boolean canOutput(Direction direction) {
        return false;
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
        return null;
    }
}
