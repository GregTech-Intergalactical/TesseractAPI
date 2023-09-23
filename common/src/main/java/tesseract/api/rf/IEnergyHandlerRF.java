package tesseract.api.rf;


import earth.terrarium.botarium.api.energy.EnergyContainer;
import earth.terrarium.botarium.api.energy.EnergySnapshot;
import earth.terrarium.botarium.api.energy.SimpleEnergySnapshot;
import tesseract.TesseractConfig;
import tesseract.api.gt.GTTransaction;
import tesseract.api.gt.IEnergyHandler;

public interface IEnergyHandlerRF extends EnergyContainer {

    IEnergyHandler getEnergyHandler();

    @Override
    default long insertEnergy(long maxAmount, boolean simulate){
        long euToInsert = (long) (maxAmount / TesseractConfig.COMMON.EU_TO_TRE_RATIO);
        long amp = getEnergyHandler().insertAmps(euToInsert, 1, simulate);
        return amp == 1 ? maxAmount : 0;
    }

    @Override
    default long extractEnergy(long maxAmount, boolean simulate){
        long euToInsert = (long) (maxAmount / TesseractConfig.COMMON.EU_TO_TRE_RATIO);
        return getEnergyHandler().extractEu(euToInsert, simulate);
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
}
