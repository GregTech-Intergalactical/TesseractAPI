package tesseract.api.capability;

import io.github.fabricators_of_create.porting_lib.util.LazyOptional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import tesseract.api.gt.*;
import tesseract.fabric.TesseractImpl;
import tesseract.graph.Graph;
import tesseract.util.Pos;

import java.util.Objects;

public class TesseractGTCapability<T extends BlockEntity & IGTCable> extends TesseractBaseCapability<T> implements IEnergyHandler {

    private final IGTCable cable;

    public TesseractGTCapability(T tile, Direction dir, boolean isNode, ITransactionModifier modifier) {
        super(tile, dir, isNode, modifier);
        this.cable = tile;
    }

    public static LazyOptional<IEnergyHandler> getGTEnergyHandler(Level level, BlockPos pos) {
        return getGTEnergyHandler(level, pos, null);
    }

    public static LazyOptional<IEnergyHandler> getGTEnergyHandler(Level level, BlockPos pos, @Nullable Direction direction) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) return LazyOptional.empty();
        return getGTEnergyHandler(be, direction);
    }

    public static LazyOptional<IEnergyHandler> getGTEnergyHandler(BlockEntity blockEntity, Direction side){
        if (blockEntity instanceof EnergyTransferable t){
            return t.getGTEnergyHandler(side);
        }
        return LazyOptional.empty();
    }

    @Override
    public boolean insert(GTTransaction transaction) {
        boolean flag = false;
        if (this.isSending) return false;
        this.isSending = true;
        long pos = tile.getBlockPos().asLong();
        if (!this.isNode) {
            long old = transaction.getAvailableAmps();
            TesseractImpl.GT_ENERGY.getController(tile.getLevel(), pos).insert(pos, side, transaction, callback);
            flag = transaction.getAvailableAmps() < old;
        } else {
            for (Direction dir : Graph.DIRECTIONS) {
                if (dir == side || !this.tile.connects(dir)) continue;
                TesseractImpl.GT_ENERGY.getController(tile.getLevel(), pos).insert(Pos.offset(pos, dir), dir.getOpposite(), transaction, callback);
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
    public long availableAmpsInput() {
        return Long.MAX_VALUE;
    }

    @Override
    public long availableAmpsOutput() {
        return Long.MAX_VALUE;
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
    public GTConsumer.State getState() {
        return new GTConsumer.State(this);
    }

    @Override
    public CompoundTag serializeNBT() {
        return null;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {

    }
}
