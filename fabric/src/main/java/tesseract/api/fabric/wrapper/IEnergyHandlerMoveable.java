package tesseract.api.fabric.wrapper;

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.EnergyExtractable;
import aztech.modern_industrialization.api.energy.EnergyInsertable;
import aztech.modern_industrialization.util.Simulation;
import tesseract.api.gt.GTTransaction;
import tesseract.api.gt.IEnergyHandler;

public interface IEnergyHandlerMoveable extends EnergyExtractable, EnergyInsertable {
    @Override
    default long extractEnergy(long maxExtract, Simulation simulation){
        GTTransaction transaction = getEnergyHandler().extract(GTTransaction.Mode.INTERNAL);
        transaction.addData(maxExtract, getEnergyHandler()::extractEnergy);
        if (simulation.isActing()) transaction.commit();
        transaction.commit();
        return transaction.isValid() ? (int) transaction.getData().stream().mapToLong(t -> t.getEnergy(t.getAmps(false), false)).sum() : 0;
    }


    @Override
    default long insertEnergy(long maxInsert, Simulation simulation){
        GTTransaction transaction = new GTTransaction(maxInsert, a -> {
        });
        getEnergyHandler().insert(transaction);
        if (simulation.isActing()) transaction.commit();
        return transaction.isValid() ? (int) transaction.getData().stream().mapToLong(t -> t.getEnergy(t.getAmps(true), true)).sum() : 0;
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
