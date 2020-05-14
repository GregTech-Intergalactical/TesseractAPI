package tesseract.graph;

import tesseract.api.IConnectable;
import tesseract.util.Dir;

import javax.annotation.ParametersAreNonnullByDefault;


/**
 * A class that acts as a controller for a connectivity of the connectables.
 */
@ParametersAreNonnullByDefault
public class Connectivity {

    /**
     * @param connectable The given instance.
     * @return Gets the connectivity for instance.
     */
    public static byte of(IConnectable connectable) {
        byte connectivity = 0;

        for (Dir direction : Dir.VALUES) {
            if (connectable.connects(direction)) {
                connectivity = Connectivity.with(connectivity, direction);
            }
        }

        return connectivity;
    }

    /**
     * @param connectivity The provided state.
     * @param side The direction index.
     * @return Connectivity state for a connection.
     */
    public static byte with(byte connectivity, Dir side) {
        return (byte) (connectivity | (1 << side.getIndex()));
    }

    /**
     * @param connectivity The provided state.
     * @param side The direction index.
     * @return True if a connection is exist, false otherwise.
     */
    public static boolean has(byte connectivity, Dir side) {
        return (connectivity & (1 << side.getIndex())) > 0;
    }
}
