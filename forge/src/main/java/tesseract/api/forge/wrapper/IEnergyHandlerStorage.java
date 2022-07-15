package tesseract.api.forge.wrapper;

import net.minecraftforge.energy.IEnergyStorage;
import tesseract.TesseractConfig;
import tesseract.api.gt.GTTransaction;
import tesseract.api.gt.IEnergyHandler;

public interface IEnergyHandlerStorage extends IEnergyStorage {
    /**
     * Forge IEnergyStorage Implementations
     **/
    @Override
    default int receiveEnergy(int maxReceive, boolean simulate) {
        GTTransaction transaction = new GTTransaction((long) (maxReceive / TesseractConfig.COMMON.EU_TO_FE_RATIO), a -> {
        });
        getEnergyHandler().insert(transaction);
        if (!simulate) transaction.commit();
        return transaction.isValid() ? (int) transaction.getData().stream().mapToLong(t -> t.getEnergy((long) (t.getAmps(true) * TesseractConfig.COMMON.EU_TO_FE_RATIO), true)).sum() : 0;
    }

    @Override
    default int extractEnergy(int maxExtract, boolean simulate) {
        GTTransaction transaction = getEnergyHandler().extract(GTTransaction.Mode.INTERNAL);
        transaction.addData((long) (maxExtract / TesseractConfig.COMMON.EU_TO_FE_RATIO), getEnergyHandler()::extractEnergy);
        if (!simulate) transaction.commit();
        return transaction.isValid() ? (int) transaction.getData().stream().mapToLong(t -> t.getEnergy((long) (t.getAmps(false) * TesseractConfig.COMMON.EU_TO_FE_RATIO), false)).sum() : 0;
    }

    @Override
    default int getEnergyStored() {
        long energy = (long) (getEnergyHandler().getEnergy() * TesseractConfig.COMMON.EU_TO_FE_RATIO);
        return energy > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) energy;
    }

    @Override
    default int getMaxEnergyStored() {
        long capacity = (long) (getEnergyHandler().getCapacity() * TesseractConfig.COMMON.EU_TO_FE_RATIO);
        return capacity > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) capacity;
    }

    @Override
    default boolean canReceive() {
        return TesseractConfig.COMMON.ENABLE_FE_OR_TRE_INPUT && getEnergyHandler().canInput();
    }

    @Override
    default boolean canExtract() {
        return getEnergyHandler().canOutput();
    }

    IEnergyHandler getEnergyHandler();
}
