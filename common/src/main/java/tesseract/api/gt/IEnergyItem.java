package tesseract.api.gt;

import tesseract.api.context.TesseractItemContext;

public interface IEnergyItem {
    IEnergyHandlerItem createEnergyHandler(TesseractItemContext context);

    default boolean canCreate(TesseractItemContext context){
        return true;
    }
}
