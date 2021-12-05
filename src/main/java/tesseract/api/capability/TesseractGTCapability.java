package tesseract.api.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import tesseract.Tesseract;
import tesseract.api.gt.GTConsumer;
import tesseract.api.gt.GTController;
import tesseract.api.gt.GTTransaction;
import tesseract.api.gt.IEnergyHandler;
import tesseract.api.gt.IGTCable;
import tesseract.graph.Graph;
import tesseract.graph.Path;
import tesseract.util.Pos;

public class TesseractGTCapability<T extends BlockEntity & IGTCable> extends TesseractBaseCapability<T> implements IEnergyHandler {

    public static final Capability<IEnergyHandler> ENERGY_HANDLER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    public static void register(RegisterCapabilitiesEvent ev) {
        ev.register(IEnergyHandler.class);
    }

    private final IGTCable cable;

    public TesseractGTCapability(T tile, Direction dir, boolean isNode, ITransactionModifier modifier) {
        super(tile, dir, isNode, modifier);
        this.cable = (IGTCable) tile;
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
            if (true) throw new IllegalStateException("For now, covers on GT Cables are disallowed");
            modifyDirs.clear();
            for (Direction dir : Graph.DIRECTIONS) {
                if (dir == this.side)
                    continue;
                if (!this.tile.connects(dir)) continue;
                BlockEntity tile = this.tile.getLevel().getBlockEntity(BlockPos.of(Pos.offset(pos, dir)));
                if (tile == null)
                    continue;
                LazyOptional<IEnergyHandler> cap = tile
                        .getCapability(ENERGY_HANDLER_CAPABILITY, dir.getOpposite());
                IEnergyHandler handle = cap.orElse(null);
                if (handle == null)
                    continue;
                int i = transaction.getData().size();
                if (handle.insert(transaction)) {
                    flag = true;
                    for (int j = i; j < transaction.getData().size(); j++) {
                        this.callback.modify(transaction.getData().get(j), this.side, dir, true);
                        transaction.getData().get(j).setLoss(transaction.getData().get(j).getLoss() + cable.getLoss());
                        GTController c = ((GTController)Tesseract.GT_ENERGY.getController(tile.getLevel(), tile.getBlockPos().asLong()));
                        transaction.pushCallback(a -> {
                            callback.modify(a, this.side, modifyDirs.pop(), false);
                            c.dataCommit(new GTConsumer(handle, Path.of(this.tile.getBlockPos().asLong(), cable, this.side, dir)), a);
                        }, j);
                        modifyDirs.add(dir);
                    }
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
    public CompoundTag serializeNBT() {
        return new CompoundTag();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {

    }

    @Override
    public GTConsumer.State getState() {
        return new GTConsumer.State(this);
    }
}
