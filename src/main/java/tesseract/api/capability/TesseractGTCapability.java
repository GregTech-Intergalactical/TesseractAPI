package tesseract.api.capability;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import tesseract.Tesseract;
import tesseract.api.Controller;
import tesseract.api.gt.GTConsumer;
import tesseract.api.gt.IEnergyHandler;
import tesseract.api.gt.IGTNode;
import tesseract.util.Dir;
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
            public long insert(long maxReceive, boolean simulate) {
                return 0;
            }

            @Override
            public long extract(long maxExtract, boolean simulate) {
                return 0;
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
            public int getOutputAmperage() {
                return 0;
            }

            @Override
            public int getOutputVoltage() {
                return 0;
            }

            @Override
            public int getInputAmperage() {
                return 0;
            }

            @Override
            public int getInputVoltage() {
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
            public boolean canInput(Dir direction) {
                return false;
            }

            @Override
            public boolean canOutput(Dir direction) {
                return false;
            }

            @Override
            public boolean connects(Dir direction) {
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
    public boolean connects(Dir direction) {
        return true;
    }

    @Override
    public long insert(long maxReceive, boolean simulate) {
        int inserted = ((Controller) Tesseract.GT_ENERGY.getController(tile.getWorld().getDimensionKey(), tile.getPos().toLong())).insert(new Pos(tile.getPos().toLong()), Dir.VALUES[side.getIndex()],maxReceive, simulate);
        return inserted;
    }

    @Override
    public long extract(long maxExtract, boolean simulate) {
        return 0;
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
    public int getOutputAmperage() {
        return 0;
    }

    @Override
    public int getOutputVoltage() {
        return 0;
    }

    @Override
    public int getInputAmperage() {
        return 0;
    }

    @Override
    public int getInputVoltage() {
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
    public boolean canInput(Dir direction) {
        return false;
    }

    @Override
    public boolean canOutput(Dir direction) {
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

    @Override
    public void refreshNet() {

    }
}
