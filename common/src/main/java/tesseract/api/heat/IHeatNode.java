package tesseract.api.heat;

import tesseract.TesseractPlatformUtils;
import tesseract.api.GraphWrapper;

public interface IHeatNode {
    HeatTransaction extract();

    void insert(HeatTransaction transaction);

    int getHeat();
    int getHeatCap();
    void update(boolean active);

    default int getTemperature() {
        return getHeat() / 100;
    }

    GraphWrapper.ICapabilityGetter<IHeatNode> GETTER = TesseractPlatformUtils::getHeatNode;
}
