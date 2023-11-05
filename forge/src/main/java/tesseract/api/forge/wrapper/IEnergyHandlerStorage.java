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
        long euToInsert = (long) (maxReceive / TesseractConfig.EU_TO_FE_RATIO.get());
        long amp = getEnergyHandler().insertAmps(euToInsert, 1, simulate);
        return amp == 1 ? maxReceive : 0;
    }

    @Override
    default int extractEnergy(int maxExtract, boolean simulate) {
        long euToInsert = (long) (maxExtract / TesseractConfig.EU_TO_FE_RATIO.get());
        return Math.toIntExact(getEnergyHandler().extractEu(euToInsert, simulate));
    }

    @Override
    default int getEnergyStored() {
        long energy = (long) (getEnergyHandler().getEnergy() * TesseractConfig.EU_TO_FE_RATIO.get());
        return energy > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) energy;
    }

    @Override
    default int getMaxEnergyStored() {
        long capacity = (long) (getEnergyHandler().getCapacity() * TesseractConfig.EU_TO_FE_RATIO.get());
        return capacity > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) capacity;
    }

    @Override
    default boolean canReceive() {
        return TesseractConfig.ENABLE_FE_OR_TRE_INPUT.get() && getEnergyHandler().canInput();
    }

    @Override
    default boolean canExtract() {
        return getEnergyHandler().canOutput();
    }

    IEnergyHandler getEnergyHandler();
}
