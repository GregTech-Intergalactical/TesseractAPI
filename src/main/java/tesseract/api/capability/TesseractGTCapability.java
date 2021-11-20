package tesseract.api.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import tesseract.Tesseract;
import tesseract.api.gt.GTConsumer;
import tesseract.api.gt.GTTransaction;
import tesseract.api.gt.IEnergyHandler;
import tesseract.graph.Graph;
import tesseract.util.Pos;

import javax.annotation.Nullable;

public class TesseractGTCapability implements IEnergyHandler {
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

    public final TileEntity tile;
    public final Direction side;

    public TesseractGTCapability(TileEntity tile, Direction dir) {
        this.tile = tile;
        this.side = dir;
    }

    @Override
    public boolean insert(GTTransaction transaction) {
        long pos = tile.getBlockPos().asLong();
        Tesseract.GT_ENERGY.getController(tile.getLevel(), pos).insert(side == null ? pos : Pos.offset(pos, Graph.DIRECTIONS[side.get3DDataValue()]), pos, transaction);
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
