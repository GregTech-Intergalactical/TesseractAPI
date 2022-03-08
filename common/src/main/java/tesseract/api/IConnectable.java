package tesseract.api;

import net.minecraft.core.Direction;
import tesseract.util.Pos;

/**
 * A simple interface for representing connectable objects.
 */
public interface IConnectable {

    /**
     * @param direction The direction vector.
     * @return True if connect to the direction, false otherwise.
     */
    boolean connects(Direction direction);

    boolean validate(Direction dir);

    default long traverse(long pos, Direction dir) {
        return Pos.offset(pos, dir);
    }

    default boolean path() {
        return false;
    }
}
