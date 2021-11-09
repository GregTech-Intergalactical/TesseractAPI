package tesseract.api;

import net.minecraft.util.Direction;

/**
 * A simple interface for representing connectable objects.
 */
public interface IConnectable {

    /**
     * @param direction The direction vector.
     * @return True if connect to the direction, false otherwise.
     */
    boolean connects(Direction direction);

    default boolean needsPath() {
        return false;
    }

    boolean validate(Direction dir);
}
