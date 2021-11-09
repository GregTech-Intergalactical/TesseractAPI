package tesseract.graph;

import net.minecraft.util.Direction;
import tesseract.api.IConnectable;

/**
 * The Cache is a class that should work with connections.
 */
public class Cache<T extends IConnectable> {

    private final byte connectivity;
    private final boolean addAsNode;
    private final T value;

    /**
     * Creates a cache instance.
     */
    public Cache(T value) {
        this.value = value;
        this.connectivity = Connectivity.of(value);
        this.addAsNode = value().needsPath();
    }

    /**
     * @param direction The direction index.
     * @return True when connect, false otherwise.
     */
    public boolean connects(Direction direction) {
        return Connectivity.has(connectivity, direction.getIndex());
    }

    /**
     * @return Gets the connection state.
     */
    public byte connectivity() {
        return connectivity;
    }

    /**
     * @return Gets the cache.
     */
    public T value() {
        return value;
    }

    /**
     * If this connector allows self-input.
     *
     * @return
     */
    public boolean registerAsNode() {
        return addAsNode;
    }
}