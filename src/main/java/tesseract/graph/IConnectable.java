package tesseract.graph;

import tesseract.util.Dir;

import javax.annotation.Nonnull;

/**
 * A simple interface for representing connectable objects.
 */
public interface IConnectable {

    /**
     * @param direction The direction vector.
     * @return True if connect to the direction, false otherwise.
     */
    boolean connects(@Nonnull Dir direction);
}
