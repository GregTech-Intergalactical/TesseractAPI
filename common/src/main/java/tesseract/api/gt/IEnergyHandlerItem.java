package tesseract.api.gt;

import org.jetbrains.annotations.NotNull;
import tesseract.api.context.TesseractItemContext;

public interface IEnergyHandlerItem extends IEnergyHandler{
    void setCapacity(long capacity);

    void setEnergy(long energy);

    @NotNull
    TesseractItemContext getContainer();
}
