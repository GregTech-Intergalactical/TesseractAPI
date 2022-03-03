package tesseract.graph;

import net.minecraft.util.Direction;
import tesseract.api.IConnectable;


/**
 * A class that acts as a controller for a connectivity of the connectables.
 */
public class Connectivity {

    /**
     * Initialize connectivity for a given instance.
     *
     * @param connectable The given instance.
     * @return Gets the connectivity for instance.
     */
    public static byte of(IConnectable connectable) {
        byte connectivity = 0;

        for (Direction direction : Direction.values()) {
            if (connectable.connects(direction)) {
                connectivity = Connectivity.set(connectivity, direction.get3DDataValue());
            }
        }

        return connectivity;
    }

    /**
     * Bitwise set operation.
     *
     * @param connectivity The provided state.
     * @param side         The side index. {@see tesseract.util.Dir}
     * @return Connectivity state for a connection.
     */
    public static byte set(byte connectivity, int side) {
        return (byte) (connectivity | (1 << side));
    }

    /**
     * Bitwise clear operation.
     *
     * @param connectivity The provided state.
     * @param side         The side index. {@see tesseract.util.Dir}
     * @return Connectivity state for a connection.
     */
    public static byte clear(byte connectivity, int side) {
        return (byte) (connectivity & ~(1 << side));
    }

    /**
     * Bitwise toggle operation.
     *
     * @param connectivity The provided state.
     * @param side         The side index. {@see tesseract.util.Dir}
     * @return Connectivity state for a connection.
     */
    public static byte toggle(byte connectivity, int side) {
        return (byte) (connectivity ^ (1 << side));
    }

    /**
     * Bitwise check operation.
     *
     * @param connectivity The provided state.
     * @param side         The side index. {@see tesseract.util.Dir}
     * @return True if a connection is exist, false otherwise.
     */
    public static boolean has(byte connectivity, int side) {
        return (connectivity & (1 << side)) > 0;
    }
}
