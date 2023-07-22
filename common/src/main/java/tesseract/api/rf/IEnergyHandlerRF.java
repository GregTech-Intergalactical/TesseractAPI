package tesseract.api.rf;

import earth.terrarium.botarium.common.energy.base.EnergyContainer;
import earth.terrarium.botarium.common.energy.base.EnergySnapshot;
import earth.terrarium.botarium.common.energy.impl.SimpleEnergySnapshot;
import tesseract.TesseractConfig;
import tesseract.api.gt.GTTransaction;
import tesseract.api.gt.IEnergyHandler;

public interface IEnergyHandlerRF extends EnergyContainer {

    IEnergyHandler getEnergyHandler();

    @Override
    default long insertEnergy(long maxAmount, boolean simulate){
        GTTransaction transaction = new GTTransaction((long) (maxAmount / TesseractConfig.COMMON.EU_TO_TRE_RATIO), a -> {
        });
        getEnergyHandler().insert(transaction);
        if (!simulate) transaction.commit();
        return transaction.isValid() ? (int) transaction.getData().stream().mapToLong(t -> t.getEnergy((long) (t.getAmps(true) * TesseractConfig.COMMON.EU_TO_TRE_RATIO), true)).sum() : 0;
    }

    @Override
    default long extractEnergy(long maxAmount, boolean simulate){
        GTTransaction transaction = getEnergyHandler().extract(GTTransaction.Mode.INTERNAL);
        transaction.addData((long) (maxAmount / TesseractConfig.COMMON.EU_TO_TRE_RATIO), getEnergyHandler()::extractEnergy);
        if (!simulate) transaction.commit();
        return transaction.isValid() ? (int) transaction.getData().stream().mapToLong(t -> t.getEnergy((long) (t.getAmps(false) * TesseractConfig.COMMON.EU_TO_TRE_RATIO), false)).sum() : 0;
    }

    @Override
    default void setEnergy(long energy){
        extractEnergy(getStoredEnergy(), false);
        insertEnergy(energy, false);
    }

    @Override
    default long getStoredEnergy(){
        return (long) (getEnergyHandler().getEnergy() * TesseractConfig.COMMON.EU_TO_TRE_RATIO);
    }

    @Override
    default long getMaxCapacity(){
        return (long) (getEnergyHandler().getCapacity() * TesseractConfig.COMMON.EU_TO_TRE_RATIO);
    }

    @Override
    default long maxInsert(){
        return (long) (getEnergyHandler().getInputVoltage() * TesseractConfig.COMMON.EU_TO_TRE_RATIO);
    }

    @Override
    default long maxExtract(){
        return (long) (getEnergyHandler().getOutputVoltage() * TesseractConfig.COMMON.EU_TO_TRE_RATIO);
    }

    @Override
    default boolean allowsInsertion(){
        return TesseractConfig.COMMON.ENABLE_FE_OR_TRE_INPUT && getEnergyHandler().canInput();
    }

    @Override
    default boolean allowsExtraction(){
        return getEnergyHandler().canOutput();
    }

    @Override
    default EnergySnapshot createSnapshot(){
        return new SimpleEnergySnapshot(this);
    }

    @Override
    default void clearContent(){
        extractEnergy(getStoredEnergy(), false);
    }
}
