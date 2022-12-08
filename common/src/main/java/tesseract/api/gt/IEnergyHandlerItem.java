package tesseract.api.gt;

import org.jetbrains.annotations.NotNull;
import tesseract.api.context.TesseractItemContext;

public interface IEnergyHandlerItem extends IEnergyHandler{
    @Override
    void setCapacity(long capacity);

    @Override
    void setEnergy(long energy);

    @NotNull
    TesseractItemContext getContainer();
}
