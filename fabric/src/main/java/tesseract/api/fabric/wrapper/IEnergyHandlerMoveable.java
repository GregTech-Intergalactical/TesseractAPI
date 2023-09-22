package tesseract.api.fabric.wrapper;

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.EnergyExtractable;
import aztech.modern_industrialization.api.energy.EnergyInsertable;
import aztech.modern_industrialization.util.Simulation;
import tesseract.TesseractConfig;
import tesseract.api.gt.IEnergyHandler;

public interface IEnergyHandlerMoveable extends EnergyExtractable, EnergyInsertable {
    @Override
    default long extractEnergy(long maxExtract, Simulation simulation){
        long euToInsert = (long) (maxExtract / TesseractConfig.COMMON.EU_TO_TRE_RATIO);
        return getEnergyHandler().extractEu(euToInsert, simulation.isSimulating());
    }


    @Override
    default long insertEnergy(long maxInsert, Simulation simulation){
        long euToInsert = (long) (maxInsert / TesseractConfig.COMMON.EU_TO_TRE_RATIO);
        long amp = getEnergyHandler().insertAmps(euToInsert, 1, simulation.isSimulating());
        return amp == 1 ? maxInsert : 0;
    }

    @Override
    default boolean canExtract(CableTier cableTier){
        return getEnergyHandler().canOutput();
    }


    @Override
    default boolean canInsert(CableTier cableTier){
        return getEnergyHandler().canInput();
    }

    IEnergyHandler getEnergyHandler();
}
