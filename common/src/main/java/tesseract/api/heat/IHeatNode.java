package tesseract.api.heat;

import net.minecraft.core.Direction;
import tesseract.TesseractPlatformUtils;
import tesseract.api.GraphWrapper;

public interface IHeatNode {
    int insert(int heat, boolean simulate);

    int extract(int heat, boolean simulate);

    int getHeat();
    int getHeatCap();
    void update(boolean active);

    /**
     * Gets if this storage can have heat extracted.
     *
     * @return If this is false, then any calls to extract will return 0.
     */
    boolean canOutput();

    /**
     * Used to determine if this storage can receive heat.
     *
     * @return If this is false, then any calls to insert will return 0.
     */
    boolean canInput();

    /**
     * Used to determine if this storage can receive heat in the given direction.
     *
     * @param direction the direction.
     * @return If this is false, then any calls to insert will return 0.
     */
    boolean canInput(Direction direction);

    /**
     * Used to determine which sides can output heat (if any).
     *
     * @param direction Direction to the output.
     * @return Returns true if the given direction is output side.
     */
    boolean canOutput(Direction direction);

    /**
     * @return The maximum amount of heat that can be inserted into the container at a time.
     */
    long getMaxInsert();

    /**
     * @return The maximum amount of heat that can be extracted from the container at a time.
     */
    long getMaxExtract();

    default int getTemperature() {
        return getHeat() / 100;
    }

    GraphWrapper.ICapabilityGetter<IHeatNode> GETTER = TesseractPlatformUtils.INSTANCE::getHeatNode;
}
