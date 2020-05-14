package tesseract.api;

import tesseract.util.Dir;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A simple interface for representing connectable objects.
 */
@ParametersAreNonnullByDefault
public interface IConnectable {

    /**
     * @param direction The direction vector.
     * @return True if connect to the direction, false otherwise.
     */
    boolean connects(Dir direction);
}
