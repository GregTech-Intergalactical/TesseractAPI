package tesseract.graph;

import tesseract.util.Dir;

import javax.annotation.Nonnull;

/**
 * The Cache is a class that should work with connections.
 */
public class Cache<C extends IConnectable> {

    private final byte connectivity;
    private final C value;

    /**
     * Creates a cache instance.
     */
    public Cache(@Nonnull C value) {
        this.value = value;
        this.connectivity = Connectivity.of(value);
    }

    /**
     * Creates a cache instance from a delegate.
     */
    public Cache(@Nonnull C value, @Nonnull IConnectable delegate) {
        this.value = value;
        this.connectivity = Connectivity.of(delegate);
    }

    /**
     * @param direction The direction index.
     * @return True when connect, false otherwise.
     */
    public boolean connects(@Nonnull Dir direction) {
        return Connectivity.has(connectivity, direction);
    }

    /**
     * @return Gets the connection state.
     */
    public byte connectivity() {
        return connectivity;
    }

    /**
     * @return Gets the cache object.
     */
    @Nonnull
    public C value() {
        return value;
    }
}