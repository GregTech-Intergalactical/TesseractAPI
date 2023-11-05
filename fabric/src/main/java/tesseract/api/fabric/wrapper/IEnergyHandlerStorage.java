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
        long euToInsert = (long) (maxReceive / TesseractConfig.EU_TO_TRE_RATIO.get());
        long amp = getEnergyHandler().insertAmps(euToInsert, 1, true);
        context.addCloseCallback((t, r) -> {
            if (r.wasCommitted()){
                getEnergyHandler().insertAmps(euToInsert, 1, false);
            }
        });
        return amp == 1 ? maxReceive : 0;
    }

    @Override
    default long extract(long maxExtract, TransactionContext context) {
        long euToExtract = (long) (maxExtract / TesseractConfig.EU_TO_TRE_RATIO.get());
        long extracted = getEnergyHandler().extractEu(euToExtract, true);
        context.addCloseCallback((t, r) -> {
            if (r.wasCommitted()){
                getEnergyHandler().extractEu(euToExtract, false);
            }
        });
        return extracted;
    }

    @Override
    default long getAmount() {
        return (long) (getEnergyHandler().getEnergy() * TesseractConfig.EU_TO_TRE_RATIO.get());
    }

    @Override
    default long getCapacity() {
        return (long) (getEnergyHandler().getCapacity() * TesseractConfig.EU_TO_TRE_RATIO.get());
    }

    @Override
    default boolean supportsInsertion() {
        return TesseractConfig.ENABLE_FE_OR_TRE_INPUT.get() && getEnergyHandler().canInput();
    }

    @Override
    default boolean supportsExtraction() {
        return getEnergyHandler().canOutput();
    }

    IEnergyHandler getEnergyHandler();
}
