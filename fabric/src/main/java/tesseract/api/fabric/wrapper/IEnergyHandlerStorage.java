package tesseract.api.fabric.wrapper;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import team.reborn.energy.api.EnergyStorage;
import tesseract.TesseractConfig;
import tesseract.api.gt.GTTransaction;
import tesseract.api.gt.IEnergyHandler;

public interface IEnergyHandlerStorage extends EnergyStorage {
    /**
     * Team Reborn EnergyStorage Implementations
     **/
    @Override
    default long insert(long maxReceive, TransactionContext context) {
        GTTransaction transaction = new GTTransaction((long) (maxReceive / TesseractConfig.COMMON.EU_TO_TRE_RATIO), a -> {
        });
        getEnergyHandler().insert(transaction);
        context.addCloseCallback((t, r) -> {
            if (r.wasCommitted()){
                transaction.commit();
            }
        });
        return transaction.isValid() ? (int) transaction.getData().stream().mapToLong(t -> t.getEnergy((long) (t.getAmps(true) * TesseractConfig.COMMON.EU_TO_TRE_RATIO), true)).sum() : 0;
    }

    @Override
    default long extract(long maxExtract, TransactionContext context) {
        GTTransaction transaction = getEnergyHandler().extract(GTTransaction.Mode.INTERNAL);
        transaction.addData((long) (maxExtract / TesseractConfig.COMMON.EU_TO_TRE_RATIO), getEnergyHandler()::extractEnergy);
        context.addCloseCallback((t, r) -> {
            if (r.wasCommitted()){
                transaction.commit();
            }
        });
        return transaction.isValid() ? (int) transaction.getData().stream().mapToLong(t -> t.getEnergy((long) (t.getAmps(false) * TesseractConfig.COMMON.EU_TO_TRE_RATIO), false)).sum() : 0;
    }

    @Override
    default long getAmount() {
        return (long) (getEnergyHandler().getEnergy() * TesseractConfig.COMMON.EU_TO_TRE_RATIO);
    }

    @Override
    default long getCapacity() {
        return (long) (getEnergyHandler().getCapacity() * TesseractConfig.COMMON.EU_TO_TRE_RATIO);
    }

    @Override
    default boolean supportsInsertion() {
        return TesseractConfig.COMMON.ENABLE_FE_OR_TRE_INPUT && getEnergyHandler().canInput();
    }

    @Override
    default boolean supportsExtraction() {
        return getEnergyHandler().canOutput();
    }

    IEnergyHandler getEnergyHandler();
}
